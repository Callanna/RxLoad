package com.ebanswers.rxdownload;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.SupportActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callanna.rxdownload.RxDownLoad;
import com.callanna.rxdownload.db.DownLoadStatus;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SignleActivity extends AppCompatActivity implements View.OnClickListener {

    String url = "http://f3.market.xiaomi.com/download/AppStore/056254c6035255f13665b88edbbc921124a4238b5/com.tencent.qqmusic.apk";
    private Button btn_download,btn_stop,btn_delete;
    private TextView tv_download;

    public static void start(Context context){
        Intent intent = new Intent(context,SignleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signle);
        btn_download= (Button) findViewById(R.id.btn_download);
        btn_stop= (Button) findViewById(R.id.btn_stop);
        btn_delete= (Button) findViewById(R.id.btn_delete);
        tv_download = (TextView) findViewById(R.id.tv_download);
        btn_download.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }
    private void todownload() {
        RxDownLoad.init(SignleActivity.this)
                .download(url)
                .subscribe(new Consumer<DownLoadStatus>() {
                    @Override
                    public void accept(@NonNull DownLoadStatus downLoadStatus) throws Exception {
                        Log.d("duanyl", "onNext: flag:"+downLoadStatus.getStatus()+",-->"+downLoadStatus.getFormatDownloadSize() + ",percent ："+downLoadStatus.getPercentNumber());
                        tv_download.setText(downLoadStatus.getStringStatus()+",   "+downLoadStatus.getFormatStatusString()+"    ,下载进度："+downLoadStatus.getPercent());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_delete:
                RxDownLoad.getInstance().delete(url);
                break;
            case R.id.btn_download:
                todownload();
                break;
            case R.id.btn_stop:
                RxDownLoad.getInstance().pause(url);
                break;
        }
    }
}
