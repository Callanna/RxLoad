package com.callanna.rxload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.callanna.rxload.api.DownloadHelper;
import com.callanna.rxload.data.LoadInfo;
import com.callanna.rxload.db.DBManager;
import com.callanna.rxload.db.DownLoadBean;
import com.callanna.rxload.db.DownLoadStatus;
import com.callanna.rxload.reader.PoiConverter;
import com.callanna.rxload.reader.tool.FileUtils;
import com.callanna.rxload.reader.tool.WebViewBridge;
import com.callanna.rxload.reader.webkit.WebActivity;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.IOException;
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
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static com.callanna.rxload.Utils.log;
import static com.callanna.rxload.db.DownLoadStatus.PAUSED;

/**
 * Created by Callanna on 2017/7/16.
 */

public class RxLoad {
    private WeakReference<Context> context;
    private static Semaphore semaphore, semaphore_prepared;
    private static DownloadHelper downloadHelper;
    private static Flowable<DownLoadBean> flowable;
    private static FlowableEmitter<DownLoadBean> flowableEmitter;
    private static Map<String, Disposable> disposableMap;
    private static DBManager dbManager;
    private static List<String> linkedList = new LinkedList();

    private static boolean isStopAll = false;


    private int maxDownloadNumber = 1;

    @SuppressLint("StaticFieldLeak")
    private volatile static RxLoad instance;

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

    private RxLoad(Context context) {
        this.context = new WeakReference<Context>(context.getApplicationContext());
        //MultiDex.install(context);
        dbManager =  DBManager.getSingleton(context);
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

    public static RxLoad init(Context context) {
        if (instance == null) {
            instance = new RxLoad(context);
        }
        Utils.setDebug(true);
        return instance;
    }

    public static RxLoad init(Context context, boolean isdebug) {
        instance = new RxLoad(context);
        Utils.setDebug(isdebug);
        return instance;
    }

    /**
     * Return RxDownload Instance
     *
     * @return RxDownload
     */
    public static RxLoad getInstance() {
        return instance;
    }

    /**
     * set default save path.
     *
     * @param savePath default save path.
     * @return instance.
     */
    public RxLoad downloadPath(String savePath) {
        downloadHelper.setDefaultSavePath(savePath);
        return this;
    }

    /**
     * set max thread to download file.
     *
     * @param max max threads
     * @return instance
     */
    public RxLoad maxThread(int max) {
        downloadHelper.setMaxThreads(max);
        return this;
    }


    /**
     * set max download number when service download
     *
     * @param max max download number
     * @return instance
     */
    public RxLoad maxDownloadNumber(int max) {
        this.maxDownloadNumber = max;
        semaphore = new Semaphore(maxDownloadNumber);
        return this;
    }

    public static synchronized void download(List<String> urls) {
        if(  instance == null){
            return;
        }
        isStopAll = false;
        Flowable.just(urls)
                .flatMap(new Function<List<String>, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(List<String> integers) throws Exception {
                        return Flowable.fromIterable(integers);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<String>() {
                    Subscription sp;

                    @Override
                    public void onSubscribe(Subscription s) {
                        sp = s;
                        sp.request(1);
                    }

                    @Override
                    public void onNext(String url) {
                        download(url, "");
                        try {
                            Thread.sleep(500);
                            sp.request(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    public static synchronized Observable<LoadInfo> download(final String url) {
        if(  instance == null){
            return null;
        }
        isStopAll = false;
        download(url, "");

        return getLoadInfo(url);
    }

    public static synchronized void download(final String url, String filename) {
        isStopAll = false;
        if (flowableEmitter != null) {
            log("onNext: 1");
            linkedList.add(url);
            downloadHelper.prepare(url, filename == null ? "" : filename)
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

    static Subscriber<DownLoadBean> subscriber_prepare = new Subscriber<DownLoadBean>() {
        Subscription subscription;

        @Override
        public void onSubscribe(Subscription s) {
            subscription = s;
            subscription.request(1);
        }

        @Override
        public void onNext(final DownLoadBean bean) {
            log("onNext: 2");
            if (isStopAll) {
                subscription.cancel();
            } else {
                log("Now is  DownLoading :" + bean.getUrl());
                if (bean.getStatus().getStatus() == DownLoadStatus.COMPLETED) {
                    File file = new File(bean.getSavePath());
                    if (file.exists()) {
                        linkedList.remove(bean.getUrl());
                        subscription.request(1);
                        log("finally  download");
                        if (!isStopAll) {
                            semaphore.release();
                        }
                    }
                }
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

    public static void start(String url) {
        if(  instance == null){
            return  ;
        }
        linkedList.add(url);
        if (flowableEmitter != null) {
            log("onNext: 1");
            linkedList.add(url);
            downloadHelper.prepare(url, "")
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

    public static void startAll() {
        if(  instance == null){
            return  ;
        }
        isStopAll = false;
        dbManager.searchDownloadByStatus(DownLoadStatus.PAUSED)
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

    public static void pause(String url) {
        if(  instance == null){
            return  ;
        }
        dbManager.updateStatusByUrl(url, PAUSED);
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public static void pauseAll() {
        if(  instance == null){
            return  ;
        }
        isStopAll = true;
        for (String url : linkedList) {
            dbManager.updateStatusByUrl(url, PAUSED);
        }
        for (Disposable each : disposableMap.values()) {
            each.dispose();
        }
    }

    public static void delete(String url) {
        if(  instance == null){
            return  ;
        }
        if (linkedList.contains(url)) {
            linkedList.remove(url);
        }
        downloadHelper.delete(dbManager.searchByUrl(url));
        Disposable disposable = disposableMap.get(url);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public static void delete(List<String> urls) {
        if(  instance == null){
            return  ;
        }
        for (int i = 0; i < urls.size(); i++) {
            delete(urls.get(i));
        }
    }

    public static void deleteAll() {
        if(  instance == null){
            return  ;
        }
        isStopAll = true;
        linkedList.clear();
        downloadHelper.deleteAll();
        if (disposableMap != null) {
            for (Disposable each : disposableMap.values()) {
                each.dispose();
            }
        }

    }

    private static DownLoadBean getDownLoadBean(String url) {
        return dbManager.searchByUrl(url);
    }

    private static String getDownLoadFilePath(String url) {
        return dbManager.searchByUrl(url).getSavePath();
    }

    private static Observable<DownLoadStatus> getDownStatus(String url) {
        Observable observer = Observable.just(url)
                .flatMap(new Function<String, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull String url) throws Exception {
                        return dbManager.searchDownloadByUrl(url)
                                .throttleLast(500, TimeUnit.MILLISECONDS);
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

    public static Observable<LoadInfo> getLoadInfo(String url) {
        if(  instance == null){
            return null;
        }
        Observable observer = Observable.just(url)
                .flatMap(new Function<String, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull String url) throws Exception {
                        return dbManager.searchDownloadByUrl(url)
                                .throttleLast(500, TimeUnit.MILLISECONDS);
                    }
                })
                .flatMap(new Function<DownLoadBean, ObservableSource<LoadInfo>>() {
                    @Override
                    public ObservableSource<LoadInfo> apply(@NonNull DownLoadBean bean) throws Exception {
                        return Observable.just(bean.toLoadInfo());
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
        return observer;
    }

    public static ObservableSource<List<LoadInfo>> getDownLoading() {
        if(  instance == null){
            return null;
        }
        return dbManager.searchDownloadByAll()
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<List<LoadInfo>>>() {
                    @Override
                    public ObservableSource<List<LoadInfo>> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                        log("getDownLoading=--->size:" + downLoadBeen.size());
                        List<LoadInfo> loadInfos = new ArrayList<>();
                        for(int i = 0; i < downLoadBeen.size();i++){
                            loadInfos.add(downLoadBeen.get(i).toLoadInfo());
                        }
                        return Observable.just(loadInfos);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static ObservableSource<List<LoadInfo>> getDownLoading(int status) {
        final List<DownLoadBean> downLoadBeanList = new ArrayList<DownLoadBean>();
        if(  instance == null){
            return null;
        }
        return dbManager.searchStatus(status)
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap(new Function<List<DownLoadBean>, ObservableSource<List<LoadInfo>>>() {
                    @Override
                    public ObservableSource<List<LoadInfo>> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                        List<LoadInfo> loadInfos = new ArrayList<>();
                        for(int i = 0; i < downLoadBeen.size();i++){
                            loadInfos.add(downLoadBeen.get(i).toLoadInfo());
                        }
                        return Observable.just(loadInfos);
                    }
                });
    }

    public static Observable<LoadInfo> loadFile(String url) {
        if(  instance == null){
            return null;
        }
        LoadInfo loadInfo = new LoadInfo();
        if (url.contains("https://") || url.contains("http://")) {
            return download(url);
        } else {
            File file = new File(url);
            if (file.exists()) {
                loadInfo.setDownloadSize(file.length());
                loadInfo.setTotalSize(file.length());
                loadInfo.setLoadurl(url);
                loadInfo.setSavePath(file.getPath());
                loadInfo.setSaveName(file.getName());
                loadInfo.setStatus(LoadInfo.COMPLETED);
            }
            return Observable.just(loadInfo);
        }
    }
    public static Observable<File> loadFile(final Context context,String url) {
        if(  instance == null){
            instance =new RxLoad(context.getApplicationContext());
        }
       return loadFile(url).flatMap(new Function<LoadInfo, ObservableSource<File>>() {
            @Override
            public ObservableSource<File> apply(LoadInfo loadInfo) throws Exception {
                Log.d("duanyl", "apply: File "+loadInfo.getStatus() +","+loadInfo.getFileExtensionName());
                File file = null;
                if(loadInfo.getStatus() == LoadInfo.COMPLETED) {
                    PoiConverter poiConverter = new PoiConverter(context);

                    if (loadInfo.getFileExtensionName().contains("doc")) {
                        file = poiConverter.docToHtmlFromSD(loadInfo.getSavePath());
                        return Observable.just(file);
                    } else if (loadInfo.getFileExtensionName().contains("xls")) {
                        file = poiConverter.xlsToHtmlFromSD(loadInfo.getSavePath());
                        return Observable.just(file);
                    } else if (loadInfo.getFileExtensionName().equals("pdf")) {
                        file = new File(loadInfo.getSavePath());
                        return Observable.just(file);
                    }
                }
                return Observable.just(file);
            }
        });
    }
    public static void openFile(final Context context, final String name){
        if(  instance == null){
            instance =new RxLoad(context.getApplicationContext());
        }
        loadFile(context,name )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
            @Override
            public void accept(File file) throws Exception {
                if(file == null){
                    return;
                }
                if(file.getName().contains(".pdf")){
                  WebActivity.openPdf(context,file.getAbsolutePath());
                }else {
                    String url  ="file://" + file.getAbsolutePath();
                    WebActivity.start(context,name);
                }

            }
        });

    }
    public static void openFileFromAssets(Context context,String name){
        if(  instance == null){
            instance =new RxLoad(context.getApplicationContext());
        }
        String url = "";
        PoiConverter poiConverter = new PoiConverter(context);
        if(name.contains(".doc")){
            File file = poiConverter.docToHtmlFromAssets(name);
            url ="file://" + file.getAbsolutePath();
            WebActivity.start(context,url);
        }else if(name.contains(".xls")){
            File file = poiConverter.xlsToHtmlFromAssets(name);
            url ="file://" + file.getAbsolutePath();
            WebActivity.start(context,url);
        }else if(name.contains(".pdf")){
            try {
                final String strOutFileName = FileUtils.getFilePath(context)+"/"+name;
                FileUtils.copyToSD(context,name,strOutFileName);
                WebActivity.openPdf(context,strOutFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static void loadFileFromAssets(String name,final WebView webView) {
        if(  instance == null){
            instance =new RxLoad(webView.getContext().getApplicationContext());
        }
        String url = "";
        PoiConverter poiConverter = new PoiConverter(webView.getContext());
        if(name.contains(".doc")){
            File file = poiConverter.docToHtmlFromAssets(name);
            url ="file://" + file.getAbsolutePath();
        }else if(name.contains(".xls")){
            File file = poiConverter.xlsToHtmlFromAssets(name);
            url ="file://" + file.getAbsolutePath();
        }else if(name.contains(".pdf")){
            url = "file:///android_asset/pdfviewer/index.html";
            try {
                final String strOutFileName = FileUtils.getRootDirectoryPath()+"/"+name;
                FileUtils.copyToSD(webView.getContext(),name,strOutFileName);
                webView.getSettings().setJavaScriptEnabled(true);//让web可以运行js
                webView.setInitialScale(100);
                webView.addJavascriptInterface(new WebViewBridge(webView.getContext()), "pdfBridge");
                webView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        webView.loadUrl("javascript:download('"+strOutFileName +"')");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        webView.loadUrl(url);
    }
    public static void loadFile(String url,final WebView webView) {
        if(  instance == null){
            instance =new RxLoad(webView.getContext().getApplicationContext());
        }
        loadFile(webView.getContext(),url ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(final File file) throws Exception {
                        String url = "";
                        if(file.getName().contains(".pdf")){
                            webView.getSettings().setJavaScriptEnabled(true);//让web可以运行js
                            webView.setInitialScale(100);
                            webView.addJavascriptInterface(new WebViewBridge(webView.getContext()), "pdfBridge");
                            url = "file:///android_asset/pdfviewer/index.html";
                            webView.setWebViewClient(new WebViewClient(){
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    webView.loadUrl("javascript:download('"+file.getAbsolutePath() +"')");
                                }
                            });
                        }else {
                            url ="file://" + file.getAbsolutePath();
                        }
                        webView.loadUrl(url);
                    }
                });

    }


    public static void loadImage( String imgUrl, ImageView imageView) {
        Glide.with(imageView.getContext()).load(imgUrl).into(imageView);
    }

    public static void loadImage( String imgUrl, int defaultResId, ImageView imageView) {
        Glide.with(imageView.getContext()).load(imgUrl).placeholder(defaultResId).into(imageView);
    }
    public static Observable<Bitmap> loadImageasBitmap(final Context context, final String imgUrl ) {
        return Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final ObservableEmitter<Bitmap> e) throws Exception {
                Glide.with(context).load(imgUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        e.onNext(resource);
                    }
                });
            }
        });
    }

    public static Observable<Drawable> loadImageAsDrawable(final Context context, final String imgUrl ) {
        return Observable.create(new ObservableOnSubscribe<Drawable>() {
            @Override
            public void subscribe(final ObservableEmitter<Drawable> e) throws Exception {
                Glide.with(context).load(imgUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        e.onNext(new BitmapDrawable(resource));
                    }
                });
            }
        });
    }
    public static void loadWeb(Context context,String url,boolean isPcClient) {
        WebActivity.start(context, url,isPcClient);
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
