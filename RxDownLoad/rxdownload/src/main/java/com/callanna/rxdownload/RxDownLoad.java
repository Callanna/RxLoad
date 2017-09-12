package com.callanna.rxdownload;

import android.annotation.SuppressLint;
import android.content.Context;

import com.callanna.rxdownload.api.DownloadHelper;
import com.callanna.rxdownload.db.DBManager;
import com.callanna.rxdownload.db.DownLoadBean;
import com.callanna.rxdownload.db.DownLoadStatus;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.callanna.rxdownload.Utils.log;
import static com.callanna.rxdownload.db.DownLoadStatus.NORMAL;
import static com.callanna.rxdownload.db.DownLoadStatus.PAUSED;

/**
 * Created by Callanna on 2017/7/16.
 */

public class RxDownLoad {
    private static final Object object = new Object();
    @SuppressLint("StaticFieldLeak")
    private volatile static RxDownLoad instance;
    static {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable instanceof InterruptedException) {
                    log("Thread interrupted");
                } else if (throwable instanceof InterruptedIOException) {
                    log("Io interrupted");
                } else if (throwable instanceof SocketException) {
                    log("Socket error");
                }
            }
        });
    }

    private int maxDownloadNumber = 1;

    private Context context;
    private Semaphore semaphore,semaphore_prepared;
    private DownloadHelper downloadHelper;

    private RxDownLoad(Context context) {
        this.context = context.getApplicationContext();
        downloadHelper = new DownloadHelper(context);
        disposableMap = new ConcurrentHashMap<>();
        semaphore = new Semaphore(maxDownloadNumber);
    }

    public static RxDownLoad init(Context context) {
        instance = new RxDownLoad(context);
        Utils.setDebug(true);
        return instance;
    }

    public static RxDownLoad init(Context context,boolean isdebug) {
        instance = new RxDownLoad(context);
        Utils.setDebug(isdebug);
        return instance;
    }

    /**
     * Return RxDownload Instance
     *
     * @return RxDownload
     */
    public static RxDownLoad getInstance() {
        return instance;
    }

    /**
     * set default save path.
     *
     * @param savePath default save path.
     * @return instance.
     */
    public RxDownLoad downloadPath(String savePath) {
        downloadHelper.setDefaultSavePath(savePath);
        return this;
    }

    /**
     * set max thread to download file.
     *
     * @param max max threads
     * @return instance
     */
    public RxDownLoad maxThread(int max) {
        downloadHelper.setMaxThreads(max);
        return this;
    }


    /**
     * set max download number when service download
     *
     * @param max max download number
     * @return instance
     */
    public RxDownLoad maxDownloadNumber(int max) {
        this.maxDownloadNumber = max;
        semaphore = new Semaphore(maxDownloadNumber);
        return this;
    }

    private Map<String, Disposable> disposableMap;
    private boolean isStopAll = false;
    private  List<String> linkedList = new LinkedList();
    private Flowable<String> flowable;
    private FlowableEmitter<String> flowableEmitter ;
    public synchronized Observable<DownLoadStatus> download(final String url) {
        if(flowable == null) {
            semaphore_prepared = new Semaphore(1);
            flowable = Flowable.create(new FlowableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                    flowableEmitter = e;
                    flowableEmitter.onNext(url);
                }
            }, BackpressureStrategy.ERROR);
            flowable.subscribe(subscriber_prepare);

        }else{
            flowableEmitter.onNext(url);
        }
        return getDownStatus(url);

    }
    Subscriber<String> subscriber_prepare = new Subscriber<String>() {
        Subscription subscription_prepare;
        @Override
        public void onSubscribe(Subscription s) {
            subscription_prepare = s;
            subscription_prepare.request(1);
        }

        @Override
        public void onNext(final String url) {
            try {
                semaphore_prepared.acquire();
                subscription_prepare.request(1);
                linkedList.add(url);
                log("Now is prepareing DownLoad :"+url);
            } catch (InterruptedException e) {
                log(e);
            }
            downloadHelper.prepare(url)
                    .subscribe(new Subscriber<DownLoadBean>() {
                Subscription subscription;
                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    subscription.request(1);
                }

                @Override
                public void onNext(final DownLoadBean bean) {
                    semaphore_prepared.release();
                    if (isStopAll) {
                        subscription.cancel();
                    }else {
                        try {
                            semaphore.acquire();
                            if (isStopAll) {
                                subscription.cancel();
                                semaphore.release();
                                return;
                            }
                            if(!linkedList.contains(bean.getUrl())){
                                semaphore.release();
                                return;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (bean.getStatus().getStatus() != NORMAL) {
                            log("Now is  DownLoading :"+bean.getUrl());
                            Disposable disposable = Observable.just(1).flatMap(new Function<Integer, ObservableSource<DownLoadStatus>>() {
                                @Override
                                public ObservableSource<DownLoadStatus> apply(@NonNull Integer integer) throws Exception {
                                    return downloadHelper.startDownLoad(bean);
                                }
                            }).observeOn(Schedulers.io())
                                    .subscribeOn(Schedulers.newThread())
                                    .doFinally(new Action() {
                                        @Override
                                        public void run() throws Exception {
                                            log("finally  download");
                                            if (!isStopAll) {
                                                semaphore.release();
                                            }
                                            if (disposableMap.size() > 0) {
                                                disposableMap.remove(bean.getUrl());
                                            }
                                            linkedList.remove(bean.getUrl());
                                        }
                                    })
                                    .doOnError(new Consumer<Throwable>() {
                                        @Override
                                        public void accept(@NonNull Throwable throwable) throws Exception {
                                            log( throwable );
                                        }
                                    }).subscribe();

                            disposableMap.put(bean.getUrl(), disposable);
                        }
                    }

                }

                @Override
                public void onError(Throwable t) {
                    log(t);
                }
                @Override
                public void onComplete() {
                    log(" prepared DownLoad :"+url);
                }
            });

        }

        @Override
        public void onError(Throwable t) {
        }
        @Override
        public void onComplete() {
        }
    };

    public void start(String url){
        linkedList.add(url);
            downloadHelper.prepare(url)
                    .subscribe(new Subscriber<DownLoadBean>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(1);
                        }
                        @Override
                        public void onNext(final DownLoadBean bean) {
                                if (bean.getStatus().getStatus() != NORMAL) {
                                    Disposable disposable = Observable.just(1).flatMap(new Function<Integer, ObservableSource<DownLoadStatus>>() {
                                        @Override
                                        public ObservableSource<DownLoadStatus> apply(@NonNull Integer integer) throws Exception {
                                            return downloadHelper.startDownLoad(bean);
                                        }
                                    }).observeOn(Schedulers.io())
                                            .subscribeOn(Schedulers.newThread())
                                            .doFinally(new Action() {
                                                @Override
                                                public void run() throws Exception {
                                                    log("finally download ");
                                                    if (!isStopAll) {
                                                        semaphore.release();
                                                    }
                                                    if (disposableMap.size() > 0) {
                                                        disposableMap.remove(bean.getUrl());
                                                    }
                                                    linkedList.remove(bean.getUrl());
                                                }
                                            })
                                            .doOnError(new Consumer<Throwable>() {
                                                @Override
                                                public void accept(@NonNull Throwable throwable) throws Exception {
                                                    log(throwable);
                                                }
                                            }).subscribe();
                                    disposableMap.put(bean.getUrl(), disposable);
                                }

                        }

                        @Override
                        public void onError(Throwable t) {
                            log(t);
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
            try {
                semaphore.acquire();
            }catch (InterruptedException e){
                log(e);
            }
    }

    public void startAll() {
        isStopAll = false;
        DBManager.getSingleton(context)
                .searchDownloadByStatus(DownLoadStatus.PAUSED)
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull List<DownLoadBean> downLoadBeens) throws Exception {
                        return Observable.fromIterable(downLoadBeens);
                    }
                }).subscribe(new Consumer<DownLoadBean>() {
            @Override
            public void accept(@NonNull DownLoadBean downLoadBean) throws Exception {
                download(downLoadBean.getUrl());
            }
        });

    }

    public void pause(String url) {
        DBManager.getSingleton(context).updateStatusByUrl(url, PAUSED);
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public void pauseAll() {
        isStopAll = true;
        for (String url : linkedList) {
            DBManager.getSingleton(context).updateStatusByUrl(url, PAUSED);
        }
        for (Disposable each : disposableMap.values()) {
            each.dispose();
        }
    }

    public void delete(String url) {
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
        if(linkedList.contains(url)){
            linkedList.remove(url);
        }
        downloadHelper.delete(DBManager.getSingleton(context).searchByUrl(url));
    }

    public void deleteAll() {
        isStopAll = true;
        if (disposableMap != null) {
            for (Disposable each : disposableMap.values()) {
                each.dispose();
            }
        }
        linkedList.clear();
        downloadHelper.deleteAll();
    }

    public Observable<DownLoadStatus> getDownStatus(String url) {
        Observable observer = Observable.just(url).flatMap(new Function<String, ObservableSource<DownLoadBean>>() {
            @Override
            public ObservableSource<DownLoadBean> apply(@NonNull String url) throws Exception {
                return DBManager.getSingleton(context).searchDownloadByUrl(url);
            }
        }).flatMap(new Function<DownLoadBean, ObservableSource<DownLoadStatus>>() {
            @Override
            public ObservableSource<DownLoadStatus> apply(@NonNull DownLoadBean bean) throws Exception {
                return Observable.just(bean.getStatus());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
        return observer;
    }

    public ObservableSource<List<DownLoadBean>> getDownLoading() {
        return DBManager.getSingleton(context).searchDownloadByAll()
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<List<DownLoadBean>>>() {
                    @Override
                    public ObservableSource<List<DownLoadBean>> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                        List<DownLoadBean> downLoadBeanList = new ArrayList<DownLoadBean>();
                        for (DownLoadBean bean : downLoadBeen) {
                                downLoadBeanList.add(bean);
                        }
                        return Observable.just(downLoadBeanList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public ObservableSource<List<DownLoadBean>> getDownLoading(int status) {
        return DBManager.getSingleton(context).searchStatus(status)
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<List<DownLoadBean>>>() {
                    @Override
                    public ObservableSource<List<DownLoadBean>> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                        List<DownLoadBean> downLoadBeanList = new ArrayList<DownLoadBean>();
                        for (DownLoadBean bean : downLoadBeen) {
                            if (bean.getStatus().getStatus() != DownLoadStatus.COMPLETED) {
                                downLoadBeanList.add(bean);
                            }
                        }
                        return Observable.just(downLoadBeanList);
                    }
                });
    }

}
