package com.ebanswers.rxdownload;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.callanna.rxdownload.RxDownLoad;
import com.callanna.rxdownload.db.DownLoadStatus;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_signle,btn_muti,btn_all;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_all= (Button) findViewById(R.id.btn_progress);
        btn_signle= (Button) findViewById(R.id.btn_signle);
        btn_muti= (Button) findViewById(R.id.btn_muti);
        btn_all.setOnClickListener(this);
        btn_signle.setOnClickListener(this);
        btn_muti.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_signle:
               SignleActivity.start(MainActivity.this);
                break;
            case R.id.btn_muti:
                RxDownLoad.init(MainActivity.this)
                .downloadPath(getCacheDir().getPath())
                .maxDownloadNumber(5)
                .maxThread(3);
                MutiActivity.start(MainActivity.this);
                break;
            case R.id.btn_progress:
                RxDownLoad.init(MainActivity.this);
                DownLoadActivity.start(MainActivity.this);
                break;
        }
    }
}
