package com.ebanswers.rxdownload;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callanna.rxdownload.RxDownLoad;
import com.callanna.rxdownload.db.DownLoadBean;
import com.callanna.rxdownload.db.DownLoadStatus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class MutiActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_download,btn_stop,btn_delete,btn_progress;
    private TextView tv_download;
    public static void start(Context context){
        Intent intent = new Intent(context,MutiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti);
        btn_download= (Button) findViewById(R.id.btn_download);
        btn_progress= (Button) findViewById(R.id.btn_progress);
        btn_stop= (Button) findViewById(R.id.btn_stop);
        btn_delete= (Button) findViewById(R.id.btn_delete);
        tv_download = (TextView) findViewById(R.id.tv_download);
        btn_download.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_progress.setOnClickListener(this);
        initData();
        RxDownLoad.getInstance().getDownLoading().subscribe(new Observer<List<DownLoadBean>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull List<DownLoadBean> downLoadBeens) {
                int waiting = 0,pause=0,starting = 0,comeplete = 0,fail = 0 ;
                for (DownLoadBean bean:downLoadBeens ) {
                    switch (bean.getStatus().getStatus()){
                        case DownLoadStatus.NORMAL:
                        case DownLoadStatus.PREPAREING:
                        case DownLoadStatus.WAITING:
                            waiting ++;
                            break;
                        case DownLoadStatus.STARTED:
                            starting++;
                            break;
                        case DownLoadStatus.PAUSED:
                            pause++;
                            break;
                        case DownLoadStatus.COMPLETED:
                            comeplete++;
                            break;
                        case DownLoadStatus.FAILED:
                            fail++;
                            break;
                    }
                }
                tv_download.setText("等待中："+waiting+",下载中："+starting+"\n" +
                        "已暂停："+pause+",已完成："+comeplete+"，下载失败："+fail);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
    private List<AppInfo> appInfoList ;
    private void initData(){
        if(appInfoList == null){
            appInfoList = new ArrayList<>();
        }
        appInfoList.add(new AppInfo("悦动圈","http://f2.market.xiaomi.com/download/AppStore/0fdde45866fcb4d662db1f504b9bb482423d44d3f/com.yuedong.sport.apk",R.mipmap.a1));
        appInfoList.add(new AppInfo("爱奇艺","http://f1.market.xiaomi.com/download/AppStore/0dec385a7f95248ac3f2a63405581b9d5fae8c328/com.qiyi.video.apk",R.mipmap.a2));
        appInfoList.add(new AppInfo("Keep","http://f5.market.mi-img.com/download/AppStore/0b77c0475dc26490b288373bbdd83760e8938a92d/com.gotokeep.keep.apk",R.mipmap.a3));
        appInfoList.add(new AppInfo("高德地图","http://amapdownload.autonavi.com/down6/C021100011760/Amap_V8.1.6.2187_android_C021100011760_(Build1709051022).apk",R.mipmap.a4));
        appInfoList.add(new AppInfo("ofo共享单车","http://f4.market.xiaomi.com/download/AppStore/09992547581001a40bd7cc26e07ac13519042ef5d/so.ofo.labofo.apk",R.mipmap.a5));
        appInfoList.add(new AppInfo("滴滴出行","http://f1.market.xiaomi.com/download/AppStore/0739d545b94364e433299fa2a2ff3d82a12eff8bb/com.sdu.didi.psnger.apk",R.mipmap.a6));
        appInfoList.add(new AppInfo("携程旅行","http://f2.market.xiaomi.com/download/AppStore/0dcfb5a2525af70b00b9e9bd99a34182784412ccc/ctrip.android.view.apk",R.mipmap.a7));
        appInfoList.add(new AppInfo("QQ"," http://f4.market.mi-img.com/download/AppStore/0ef745dfdef0157c80cd3eb64ecd36a9f17403341/com.tencent.mobileqq.apk",R.mipmap.a8));
        appInfoList.add(new AppInfo("微信"," http://f4.market.mi-img.com/download/AppStore/065fc45210ea8e141fe49042b4cfd4480fe41af7f/com.tencent.mm.apk",R.mipmap.a9));
        appInfoList.add(new AppInfo("微博","http://f5.market.mi-img.com/download/AppStore/09baca46e8a13404a29cd051dbd83bf72ea6c4082/com.sina.weibo.apk",R.mipmap.a10));

    }
    private void todownload() {
        for (AppInfo appinfo : appInfoList) {
            RxDownLoad.getInstance().download(appinfo.getUrl());
        }

    }
    boolean isStop= false;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_delete:
                isStop= true;
                RxDownLoad.getInstance().deleteAll();
                break;
            case R.id.btn_download:
                isStop= false;
                todownload();
                break;
            case R.id.btn_stop:
                if(isStop){
                    isStop = false;
                    RxDownLoad.getInstance().startAll();
                }else {
                    isStop = true;
                    RxDownLoad.getInstance().pauseAll();
                }
                break;
            case R.id.btn_progress:
                DownLoadActivity.start(MutiActivity.this);
                break;
        }
    }


}
