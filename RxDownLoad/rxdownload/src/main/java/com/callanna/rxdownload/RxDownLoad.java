package com.callanna.rxdownload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.callanna.rxdownload.api.DownloadHelper;
import com.callanna.rxdownload.db.DBManager;
import com.callanna.rxdownload.db.DownLoadBean;
import com.callanna.rxdownload.db.DownLoadStatus;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.callanna.rxdownload.Utils.log;
import static com.callanna.rxdownload.db.DownLoadStatus.CANCELED;
import static com.callanna.rxdownload.db.DownLoadStatus.NORMAL;
import static com.callanna.rxdownload.db.DownLoadStatus.PAUSED;
import static com.callanna.rxdownload.db.DownLoadStatus.PREPAREING;

/**
 * Created by Callanna on 2017/7/16.
 */

public class RxDownLoad {
    private static final Object object = new Object();
    @SuppressLint("StaticFieldLeak")
    private volatile static RxDownLoad instance;
    private volatile static boolean bound = false;

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

    private int maxDownloadNumber = 3;

    private Context context;
    //private Semaphore semaphore;
    private DownloadHelper downloadHelper;

    private RxDownLoad(Context context) {
        this.context = context.getApplicationContext();
        downloadHelper = new DownloadHelper(context);
        // semaphore = new Semaphore(maxDownloadNumber);
    }
    public static RxDownLoad init(Context context){
        instance = new RxDownLoad(context);
        return  instance;
    }
    /**
     * Return RxDownload Instance
     *
     * @param context context
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
        // semaphore = new Semaphore(maxDownloadNumber);
        return this;
    }

    private Map<String, Disposable> disposableMap;
    private boolean isStopAll = false;

    public synchronized Observable<DownLoadStatus> download(final String url) {
        Log.d("duanyl", "download: ");
        if (disposableMap == null) {
            disposableMap = new ConcurrentHashMap();
        }
        downloadHelper.prepare(url)
                .subscribe(new Subscriber<DownLoadBean>() {
                    Subscription subscription;
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d("duanyl", "onSubscribe: ");
                        subscription = s;
                        subscription.request(maxDownloadNumber);
                    }

                    @Override
                    public void onNext(final DownLoadBean bean) {

                        Log.d("duanyl", "onNext: ");
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
                                            log("finally and release...");
                                            if (disposableMap.size() > 0) {
                                                disposableMap.remove(url);
                                            }
                                            subscription.request(1);
                                        }
                                    })
                                    .doOnError(new Consumer<Throwable>() {
                                        @Override
                                        public void accept(@NonNull Throwable throwable) throws Exception {
                                            throwable.printStackTrace();
                                            log("startDownload  " + throwable.getMessage());
                                        }
                                    }).subscribe();

                            disposableMap.put(url, disposable);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log(t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("duanyl", "onComplete: ");
                    }
                });
        return getDownStatus(url);

    }

    private void startOtherWaiting() {
        DBManager.getSingleton(context).searchDownloadByStatus(DownLoadStatus.WAITING).flatMap(new Function<List<DownLoadBean>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                if (downLoadBeen.size() > 0) {
                    return null;
                }
                for (DownLoadBean bean : downLoadBeen) {
                    download(downLoadBeen.get(0).getUrl());
                }
                return null;
            }
        });
    }

    public void start(DownLoadBean bean) {
        download(bean.getUrl());
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
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
            DBManager.getSingleton(context).updateStatusByUrl(url, PAUSED);
        }
    }

    public void pauseAll() {
        isStopAll = true;
        Iterator iterator = disposableMap.keySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Disposable val = (Disposable) entry.getValue();
            val.dispose();
            DBManager.getSingleton(context).updateStatusByUrl(key, PAUSED);
        }
    }

    public void delete(String url) {
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
            DBManager.getSingleton(context).updateStatusByUrl(url, CANCELED);
        }
        downloadHelper.delete(url);
    }

    public void deleteAll() {
        isStopAll = true;
        Iterator iterator = disposableMap.keySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Disposable val = (Disposable) entry.getValue();
            val.dispose();
            DBManager.getSingleton(context).updateStatusByUrl(key, CANCELED);
        }
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
                log("getDownStatus   cc ");
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
                            if (bean.getStatus().getStatus() != DownLoadStatus.COMPLETED) {
                                downLoadBeanList.add(bean);
                            }
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
