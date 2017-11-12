package com.ebanswers.rxdownload;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.callanna.rxload.RxLoad;

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
        Log.d("duanyl", "onCreate: "+Environment.getExternalStoragePublicDirectory("download").getPath());
        Log.d("duanyl", "onCreate: "+getApplicationContext().getCacheDir().getPath());
        Log.d("duanyl", "onCreate: "+getApplicationContext().getFilesDir().getPath());
        RxLoad.init(MainActivity.this)
                .downloadPath(Environment.getExternalStoragePublicDirectory("download").getPath())
                .maxDownloadNumber(3)
                .maxThread(3);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_signle:

               SignleActivity.start(MainActivity.this);
                break;
            case R.id.btn_muti:

                MutiActivity.start(MainActivity.this);
                break;
        }
    }
}
