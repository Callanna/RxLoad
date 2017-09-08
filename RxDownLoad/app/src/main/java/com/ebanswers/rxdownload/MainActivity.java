package com.ebanswers.rxdownload;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.callanna.rxdownload.RxDownLoad;
import com.callanna.rxdownload.db.DownLoadStatus;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    String url = "http://storage.56iq.net/group1/M00/16/84/CgoKRFlxvHqAJiokAMwSIOoB0YI617.apk";
    private Button btn_download;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_download= (Button) findViewById(R.id.btn_download);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RxDownLoad.getInstance(MainActivity.this).download(url);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        RxDownLoad.getInstance(MainActivity.this).getDownStatus(url).subscribe(new Observer<DownLoadStatus>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DownLoadStatus downLoadStatus) {
                Log.d("duanyl", "onNext: flag:"+downLoadStatus.getStatus()+",-->"+downLoadStatus.getFormatDownloadSize() + ",percent ："+downLoadStatus.getPercentNumber());
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
