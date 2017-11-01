package com.callanna.rxdownload;

import android.annotation.SuppressLint;
import android.content.Context;

import com.callanna.rxdownload.api.DownloadHelper;
import com.callanna.rxdownload.db.DBManager;
import com.callanna.rxdownload.db.DownLoadBean;
import com.callanna.rxdownload.db.DownLoadStatus;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
import static com.callanna.rxdownload.db.DBManager.getSingleton;
import static com.callanna.rxdownload.db.DownLoadStatus.PAUSED;

/**
 * Created by Callanna on 2017/7/16.
 */

public class RxDL {
    private WeakReference<Context> context;
    private static Semaphore semaphore, semaphore_prepared;
    private static DownloadHelper downloadHelper;
    private static Flowable<DownLoadBean> flowable;
    private static FlowableEmitter<DownLoadBean> flowableEmitter;
    private static Map<String, Disposable> disposableMap;

    private static List<String> linkedList = new LinkedList();

    private static boolean isStopAll = false;


    private int maxDownloadNumber = 1;

    @SuppressLint("StaticFieldLeak")
    private volatile static RxDL instance;

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

    private RxDL(Context context) {
        this.context = new WeakReference<Context>(context.getApplicationContext());

        downloadHelper = new DownloadHelper(context);
        disposableMap = new ConcurrentHashMap<>();
        semaphore = new Semaphore(maxDownloadNumber);
        semaphore_prepared = new Semaphore(1);
        if (flowable == null) {

            flowable = Flowable.create(new FlowableOnSubscribe<DownLoadBean>() {
                @Override
                public void subscribe(@NonNull FlowableEmitter<DownLoadBean> e) throws Exception {
                    flowableEmitter = e;
                }
            }, BackpressureStrategy.ERROR);

            flowable.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread()).subscribe(subscriber_prepare);
        }
    }

    public static RxDL init(Context context) {
        if (instance == null) {
            instance = new RxDL(context);
        }
        Utils.setDebug(true);
        return instance;
    }

    public static RxDL init(Context context, boolean isdebug) {
        instance = new RxDL(context);
        Utils.setDebug(isdebug);
        return instance;
    }

    /**
     * Return RxDownload Instance
     *
     * @return RxDownload
     */
    public static RxDL getInstance() {
        return instance;
    }

    /**
     * set default save path.
     *
     * @param savePath default save path.
     * @return instance.
     */
    public RxDL downloadPath(String savePath) {
        downloadHelper.setDefaultSavePath(savePath);
        return this;
    }

    /**
     * set max thread to download file.
     *
     * @param max max threads
     * @return instance
     */
    public RxDL maxThread(int max) {
        downloadHelper.setMaxThreads(max);
        return this;
    }


    /**
     * set max download number when service download
     *
     * @param max max download number
     * @return instance
     */
    public RxDL maxDownloadNumber(int max) {
        this.maxDownloadNumber = max;
        semaphore = new Semaphore(maxDownloadNumber);
        return this;
    }
    public synchronized void download(List<String> urls) {

        Flowable.just(urls)
                .flatMap(new Function<List<String>, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(List<String> integers) throws Exception {
                        return Flowable.fromIterable(integers);
                    }
                }).subscribe(new Subscriber<String>() {
            Subscription sp;
            @Override
            public void onSubscribe(Subscription s) {
                sp = s;
                sp.request(1);
            }

            @Override
            public void onNext(String url) {
               download(url,"");
               Observable.timer(300,TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
                   @Override
                   public void accept(@NonNull Long aLong) throws Exception {
                       sp.request(1);
                   }
               });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });


    }

    public synchronized Observable<DownLoadStatus> download(final String url) {
        return download(url,"");
    }
    public synchronized Observable<DownLoadStatus> download(final String url,String filename) {
        if (flowableEmitter != null) {
            log("onNext: 1");
            linkedList.add(url);
            downloadHelper.prepare(url,filename == null?"":filename)
                    .subscribe(new Subscriber<DownLoadBean>() {
                        Subscription subscription;

                        @Override
                        public void onSubscribe(Subscription s) {
                            subscription = s;
                            subscription.request(1);
                        }

                        @Override
                        public void onNext(final DownLoadBean bean) {
                            flowableEmitter.onNext(bean);
                        }

                        @Override
                        public void onError(Throwable t) {
                            log(t);
                            log(" prepared onError");
                        }

                        @Override
                        public void onComplete() {
                            log(" prepared onComplete");

                        }

                    });
        }
        return getDownStatus(url);

    }

    static Subscriber<DownLoadBean> subscriber_prepare = new Subscriber<DownLoadBean>() {
        Subscription subscription;

        @Override
        public void onSubscribe(Subscription s) {
            subscription  = s;
            subscription .request(1);
        }

        @Override
        public void onNext(final DownLoadBean bean) {
            log("onNext: 2");
            if (isStopAll) {
                subscription.cancel();
            } else {
                    log("Now is  DownLoading :" + bean.getUrl());
                    Disposable disposable = Observable.just(1)
                            .flatMap(new Function<Integer, ObservableSource<DownLoadStatus>>() {
                                @Override
                                public ObservableSource<DownLoadStatus> apply(@NonNull Integer integer) throws Exception {
                                    return downloadHelper.startDownLoad(bean);
                                }
                            })
                            .observeOn(Schedulers.io())
                            .subscribeOn(Schedulers.newThread())
                            .doOnComplete(new Action() {
                                @Override
                                public void run() throws Exception {
                                    linkedList.remove(bean.getUrl());
                                }
                            })
                            .doOnError(new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable throwable) throws Exception {
                                    log(throwable);
                                }
                            })
                            .doFinally(new Action() {
                                @Override
                                public void run() throws Exception {
                                    //下载结束，请求下一个下载任务
                                    subscription.request(1);
                                    log("finally  download");
                                    if (!isStopAll) {
                                        semaphore.release();
                                    }
                                    if (disposableMap.size() > 0) {
                                        disposableMap.remove(bean.getUrl());
                                    }
                                }
                            }).subscribe();

                    disposableMap.put(bean.getUrl(), disposable);
                }
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    };

    public void start(String url) {
        linkedList.add(url);
        if (flowableEmitter != null) {
            log("onNext: 1");
            linkedList.add(url);
            downloadHelper.prepare(url,"")
                    .subscribe(new Subscriber<DownLoadBean>() {
                        Subscription subscription;

                        @Override
                        public void onSubscribe(Subscription s) {
                            subscription = s;
                            subscription.request(1);
                        }

                        @Override
                        public void onNext(final DownLoadBean bean) {
                            flowableEmitter.onNext(bean);
                        }

                        @Override
                        public void onError(Throwable t) {
                            log(t);
                            log(" prepared onError");
                        }

                        @Override
                        public void onComplete() {
                            log(" prepared onComplete");

                        }

                    });
        }
    }

    public void startAll() {
        isStopAll = false;
        getSingleton(context.get())
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
        getSingleton(context.get()).updateStatusByUrl(url, PAUSED);
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public void pauseAll() {
        isStopAll = true;
        for (String url : linkedList) {
            getSingleton(context.get()).updateStatusByUrl(url, PAUSED);
        }
        for (Disposable each : disposableMap.values()) {
            each.dispose();
        }
    }

    public void delete(String url) {
        if (linkedList.contains(url)) {
            linkedList.remove(url);
        }
        downloadHelper.delete(getSingleton(context.get()).searchByUrl(url));
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
    }
    public void delete(List<String> urls) {
         for (int i = 0; i < urls.size();i++){
             delete(urls.get(i));
         }
    }
    public void deleteAll() {
        isStopAll = true;
        linkedList.clear();
        downloadHelper.deleteAll();
        if (disposableMap != null) {
            for (Disposable each : disposableMap.values()) {
                each.dispose();
            }
        }

    }

    public DownLoadBean getDownLoadBean(String url) {
        return DBManager.getSingleton(context.get()).searchByUrl(url);
    }

    public String getDownLoadFilePath(String url) {
        return DBManager.getSingleton(context.get()).searchByUrl(url).getSavePath();
    }

    public Observable<DownLoadStatus> getDownStatus(String url) {
        Observable observer = Observable.just(url)
                .flatMap(new Function<String, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull String url) throws Exception {
                        return getSingleton(context.get()).searchDownloadByUrl(url)
                                .throttleLast(1, TimeUnit.SECONDS);
                    }
                })
                .flatMap(new Function<DownLoadBean, ObservableSource<DownLoadStatus>>() {
                    @Override
                    public ObservableSource<DownLoadStatus> apply(@NonNull DownLoadBean bean) throws Exception {
                        return Observable.just(bean.getStatus());
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
        return observer;
    }

    public ObservableSource<List<DownLoadBean>> getDownLoading() {
        return getSingleton(context.get()).searchDownloadByAll()
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<List<DownLoadBean>>>() {
                    @Override
                    public ObservableSource<List<DownLoadBean>> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                        log("getDownLoading=--->size:" + downLoadBeen.size());
                        return Observable.just(downLoadBeen);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public ObservableSource<List<DownLoadBean>> getDownLoading(int status) {
        final List<DownLoadBean> downLoadBeanList = new ArrayList<DownLoadBean>();
        return getSingleton(context.get()).searchStatus(status)
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<List<DownLoadBean>>>() {
                    @Override
                    public ObservableSource<List<DownLoadBean>> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                        log("getDownLoading=--->size:" + downLoadBeen.size());
                        return Observable.just(downLoadBeen);
                    }
                });
    }

    public static void release() {
        if (instance != null) {
            linkedList.clear();
            isStopAll = false;
            semaphore.release();
            semaphore_prepared.release();
            semaphore = null;
            semaphore_prepared = null;
            downloadHelper = null;
            subscriber_prepare.onComplete();
            flowable = null;
            flowableEmitter = null;
            disposableMap = null;
        }
    }
}
