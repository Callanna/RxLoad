package com.callanna.rxdownload.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.callanna.rxdownload.Utils;
import com.callanna.rxdownload.db.DBManager;
import com.callanna.rxdownload.db.DownLoadBean;
import com.callanna.rxdownload.db.DownLoadStatus;
import com.callanna.rxdownload.db.DownloadRange;
import com.callanna.rxdownload.file.FileHelper;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.callanna.rxdownload.Utils.NORMAL_RETRY_HINT;
import static com.callanna.rxdownload.Utils.RANGE_RETRY_HINT;
import static com.callanna.rxdownload.Utils.REQUEST_RETRY_HINT;
import static com.callanna.rxdownload.Utils.empty;
import static com.callanna.rxdownload.Utils.fileName;
import static com.callanna.rxdownload.Utils.formatStr;
import static com.callanna.rxdownload.Utils.log;
import static com.callanna.rxdownload.Utils.mkdirs;
import static com.callanna.rxdownload.db.DownLoadStatus.COMPLETED;
import static com.callanna.rxdownload.db.DownLoadStatus.PREPAREING;
import static com.callanna.rxdownload.db.DownLoadStatus.WAITING;
import static java.io.File.separator;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/2
 * Time: 09:39
 * Download helper
 */
public class DownloadHelper {
    public static final String TEST_RANGE_SUPPORT = "bytes=0-";
    private static final CharSequence CACHE = "cache";
    public static final String TMP_SUFFIX = ".tmp";  //temp file
    public static final String LMF_SUFFIX = ".lmf";  //last modify file
    private int maxRetryCount = 3;
    private int maxThreads = 3;
    private String defaultSavePath = "";
    private String cachePath = "";

    private FileHelper fileHelper;
    private DownloadApi downloadApi;
    private DBManager dbManager;

    public DownloadHelper(Context context) {
        downloadApi = RetrofitProvider.getInstance().create(DownloadApi.class);
        defaultSavePath = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        dbManager = DBManager.getSingleton(context.getApplicationContext());
        fileHelper = new FileHelper(maxThreads);

        cachePath = TextUtils.concat(defaultSavePath, separator, CACHE).toString();
        mkdirs(defaultSavePath, cachePath);


    }

    public void setRetrofit(Retrofit retrofit) {
        downloadApi = retrofit.create(DownloadApi.class);
    }

    public void setDefaultSavePath(String defaultSavePath) {
        this.defaultSavePath = defaultSavePath;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }


    /**
     * prepare normal download, create files and save last-modify.
     *
     * @throws IOException
     * @throws ParseException
     */
    public void prepareNormalDownload(DownLoadBean bean) throws IOException, ParseException {
        fileHelper.prepareDownload(new File(bean.getLmfPath()), new File(bean.getSavePath()), bean.getStatus().getTotalSize());
    }

    /**
     * prepare range download, create necessary files and save last-modify.
     *
     * @throws IOException
     * @throws ParseException
     */
    public void prepareRangeDownload(DownLoadBean bean) throws IOException, ParseException {
        fileHelper.prepareDownload(new File(bean.getLmfPath()),new File(bean.getTempPath()), new File(bean.getSavePath()),  bean.getStatus().getTotalSize());
    }

    /**
     * Read download range from record file.
     *
     * @param index index
     * @return
     * @throws IOException
     */
    public DownloadRange readDownloadRange(File temp,int index) throws IOException {
        return fileHelper.readDownloadRange(temp, index);
    }


    /**
     * Normal download save.
     *
     * @param e        emitter
     * @param response response
     */
    public void save(FlowableEmitter<DownLoadStatus> e,String path, ResponseBody response) {

        fileHelper.saveFile(e, new File(path), response);
    }

    /**
     * Range download save
     *
     * @param emitter  emitter
     * @param index    download index
     * @param response response
     * @throws IOException
     */
    public void save(FlowableEmitter<DownLoadStatus> emitter,String path,String tpath, int index, ResponseBody response)
            throws IOException {
        fileHelper.saveFile(emitter, index, new File(tpath), new File(path), response);
    }

    /**
     * Normal download request.
     *
     * @return response
     */
    public Publisher<DownLoadStatus> download(DownLoadBean bean) throws InterruptedException {
        if (bean.getIsSupportRange()) {
            List<Publisher<DownLoadStatus>> tasks = new ArrayList<>();
            for (int i = 0; i < maxThreads; i++) {
                TimeUnit.MILLISECONDS.sleep(300);
                tasks.add(rangeDownload(i, bean));
            }
            return Flowable.mergeDelayError(tasks);
        } else {
            return download(bean.getUrl(),bean.getSavePath());
        }
    }

    /**
     * Normal download request.
     *
     * @return response
     */
    public Publisher<DownLoadStatus> download(String url,final String path) {

        return downloadApi.download(null, url)
                .subscribeOn(Schedulers.io())  //Important!
                .flatMap(new Function<Response<ResponseBody>, Publisher<DownLoadStatus>>() {
                    @Override
                    public Publisher<DownLoadStatus> apply(final Response<ResponseBody> response) throws Exception {
                        return save(path,"",-1,response.body());
                    }
                }).compose(Utils.<DownLoadStatus>retry2(NORMAL_RETRY_HINT,maxRetryCount));
    }

    /**
     * Range download request
     *
     * @param index download index
     * @return response
     */
    public Publisher<DownLoadStatus> rangeDownload(final int index, final DownLoadBean bean) {
        return Flowable
                .create(new FlowableOnSubscribe<DownloadRange>() {
                    @Override
                    public void subscribe(FlowableEmitter<DownloadRange> e) throws Exception {
                        DownloadRange range = readDownloadRange(new File(bean.getTempPath()),index);
                        if (range.legal()) {
                            e.onNext(range);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.ERROR)
                .flatMap(new Function<DownloadRange, Publisher<Response<ResponseBody>>>() {
                    @Override
                    public Publisher<Response<ResponseBody>> apply(DownloadRange range)
                            throws Exception {
                        String rangeStr = "bytes=" + range.start + "-" + range.end;
                        log("rangeDownload--->" + rangeStr);
                        return downloadApi.download(rangeStr, bean.getUrl());
                    }
                })
                .flatMap(new Function<Response<ResponseBody>, Publisher<DownLoadStatus>>() {
                    @Override
                    public Publisher<DownLoadStatus> apply(Response<ResponseBody> response) throws Exception {
                        return save(bean.getSavePath(),bean.getTempPath(),index, response.body());
                    }
                })
                .subscribeOn(Schedulers.io())  //Important!;
                .compose(Utils.<DownLoadStatus>retry2(formatStr(RANGE_RETRY_HINT, index),maxRetryCount));

    }

    /**
     * 保存断点下载的文件,以及下载进度
     *
     * @param index    下载编号
     * @param response 响应值
     * @return Flowable
     */
    private Publisher<DownLoadStatus> save(final String path, final String tpath, final int index, final ResponseBody response) {

        Flowable<DownLoadStatus> flowable = Flowable.create(new FlowableOnSubscribe<DownLoadStatus>() {
            @Override
            public void subscribe(FlowableEmitter<DownLoadStatus> emitter) throws Exception {
                if (index == -1) {
                    save(emitter,path , response);
                } else {
                    save(emitter,path,tpath, index, response);
                }
            }
        }, BackpressureStrategy.LATEST)
                .replay(1)
                .autoConnect();
        return flowable.throttleFirst(200, TimeUnit.MILLISECONDS).mergeWith(flowable.takeLast(1))
                .subscribeOn(Schedulers.newThread());
    }
    public Flowable<DownLoadBean> prepare(final String url) {
        Flowable flowable = Flowable.create(new FlowableOnSubscribe<DownLoadBean>() {
            @Override
            public void subscribe(@NonNull final FlowableEmitter<DownLoadBean> e) throws Exception {
                Observable.just(1).flatMap(new Function<Integer, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull Integer integer) throws Exception {
                        DownLoadBean downLoadBean = dbManager.searchByUrl(url);
                        if (downLoadBean.getSaveName() == null || downLoadBean.getSaveName().equals("")) {
                            return checkRange(downLoadBean);
                        } else {
                            return checkFile(downLoadBean, downLoadBean.getLastModify());
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new Consumer<DownLoadBean>() {
                            @Override
                            public void accept(@NonNull DownLoadBean bean) throws Exception {
                                e.onNext(bean);
                                e.onComplete();
                            }
                        });

            }
        }, BackpressureStrategy.ERROR);
        return flowable;
    }

    public ObservableSource<DownLoadStatus> startDownLoad(final DownLoadBean bean) {
        Log.d("duanyl", "startDownLoad: ");
        return Flowable.just(1)
                .flatMap(new Function<Integer, Publisher<DownLoadStatus>>() {
                    @Override
                    public Publisher<DownLoadStatus> apply(@NonNull Integer integer) throws Exception {
                        return prepareDownLoad(bean);
                    }
                })
                .map(new Function<DownLoadStatus, DownLoadStatus>() {
                    @Override
                    public DownLoadStatus apply(@NonNull DownLoadStatus downLoadStatus) throws Exception {
                        dbManager.updateStatusByUrl(bean.getUrl(), downLoadStatus);
                        return downLoadStatus;
                    }
                })
                .flatMap(new Function<DownLoadStatus, Publisher<DownLoadStatus>>() {
                    @Override
                    public Publisher<DownLoadStatus> apply(@NonNull DownLoadStatus downLoadStatus) throws Exception {
                        return download(bean);
                    }
                })
                .subscribeOn(Schedulers.io())
                .map(new Function<DownLoadStatus, DownLoadStatus>() {
                    @Override
                    public DownLoadStatus apply(@NonNull DownLoadStatus downLoadStatus) throws Exception {
                        dbManager.updateStatusByUrl(bean.getUrl(), downLoadStatus);
                        return downLoadStatus;
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        log(throwable);
                        dbManager.updateStatusByUrl(bean.getUrl(), DownLoadStatus.FAILED);
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        dbManager.updateStatusByUrl(bean.getUrl(), DownLoadStatus.COMPLETED);
                    }
                })
                .toObservable();
    }

    private Publisher<DownLoadStatus> prepareDownLoad(DownLoadBean bean) {
        if(bean.getStatus().getStatus()==PREPAREING) {
            try {
                if (bean.getIsSupportRange()) {
                    prepareRangeDownload(bean);
                } else {
                    prepareNormalDownload(bean);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        bean.getStatus().setStatus(WAITING);
        return Flowable.just(bean.getStatus());
    }

    /**
     * http checkRangeByHead request,checkRange need info.
     *
     * @return empty Observable
     */
    private ObservableSource<DownLoadBean> checkRange(final DownLoadBean bean) {
        return downloadApi.checkRangeByHead(TEST_RANGE_SUPPORT, bean.getUrl())
                .flatMap(new Function<Response<Void>, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull Response<Void> response) throws Exception {
                        if (response.isSuccessful()) {
                            saveFileInfo(bean, response, PREPAREING);
                            bean.setIsSupportRange(!Utils.notSupportRange(response));
                            if (dbManager != null) {
                                dbManager.add(bean);
                            }
                            log("checkRange accept: " + bean.getIsSupportRange());
                        }
                        return Observable.just(bean);
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                })
                .compose(Utils.<DownLoadBean>retry(formatStr(REQUEST_RETRY_HINT),maxRetryCount));
    }

    /**
     * http checkRangeByHead request,checkRange need info, check whether if server file has changed.
     *
     * @return empty Observable
     */
    private ObservableSource<DownLoadBean> checkFile(final DownLoadBean bean, final String lastModify) {
        return downloadApi.checkFileByHead(lastModify, bean.getUrl())
                .flatMap(new Function<Response<Void>, ObservableSource<DownLoadBean>>() {
                    @Override
                    public ObservableSource<DownLoadBean> apply(@NonNull Response<Void> response) throws Exception {
                        log("accept: checkFile" + response.code());
                        if (response.code() == 200) {
                            //如果时间一致，那么返回HTTP状态码304,如果 改变了 返回200
                            delete(bean);
                            return checkRange(bean);
                        }else{
                            return Observable.just(bean);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .compose(Utils.<DownLoadBean>retry(formatStr(REQUEST_RETRY_HINT),maxRetryCount));

    }

    public void delete(DownLoadBean bean) {
        if(bean != null) {
            dbManager.clearStatusByUrl(bean.getUrl());
            new File(bean.getSavePath()).delete();
            new File(bean.getTempPath()).delete();
            new File(bean.getLmfPath()).delete();
        }
    }

    public void deleteAll() {
        dbManager.searchDownloadByAll().flatMap(new Function<List<DownLoadBean>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(@NonNull List<DownLoadBean> downLoadBeen) throws Exception {
                for (DownLoadBean bean : downLoadBeen) {
                    if(bean.getStatus().getStatus() != COMPLETED) {
                        delete(bean);
                    }
                }
                return null;
            }
        });
    }
    /**
     * Save file info
     *
     * @param response response
     */
    public void saveFileInfo(DownLoadBean bean, Response<?> response, int flag) {
        DownLoadStatus downLoadStatus = new DownLoadStatus(flag);
        if (empty(bean.getSaveName())) {
            bean.setSaveName(fileName(bean.getUrl(), response));
            bean.setSavePath(defaultSavePath.toString()+"/"+bean.getSaveName());
            bean.setTempPath(cachePath + File.separator + bean.getFileName()+ TMP_SUFFIX);
            bean.setLmfPath(cachePath + separator + bean.getFileName() + LMF_SUFFIX);
        }
        Log.d("duanyl", "saveFilePath: " + bean.getSavePath());
        downLoadStatus.setTotalSize(Utils.contentLength(response));
        bean.setStatus(downLoadStatus);
        bean.setLastModify((Utils.lastModify(response)));
    }
}
