package com.ebanswers.rxdownload;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.callanna.rxdownload.RxDownLoad;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_signle,btn_muti ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_signle= (Button) findViewById(R.id.btn_signle);
        btn_muti= (Button) findViewById(R.id.btn_muti);
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
                .maxDownloadNumber(3)
                .maxThread(3);
                MutiActivity.start(MainActivity.this);
                break;
        }
    }
}
