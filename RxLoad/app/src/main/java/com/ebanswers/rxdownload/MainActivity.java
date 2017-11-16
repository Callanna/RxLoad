package com.ebanswers.rxdownload;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.callanna.rxload.RxLoad;

import static com.ebanswers.rxdownload.R.id.btn_opendoc;
import static com.ebanswers.rxdownload.R.id.btn_openxls;
import static com.ebanswers.rxdownload.R.id.btn_openyouku;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_signle,btn_muti ,btn_doc,btn_xls,btn_web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_signle= (Button) findViewById(R.id.btn_signle);
        btn_muti= (Button) findViewById(R.id.btn_muti);
        btn_doc = (Button) findViewById(btn_opendoc);
        btn_xls = (Button)findViewById(btn_openxls);
        btn_web = (Button)findViewById(btn_openyouku);

        btn_doc.setOnClickListener(this);
        btn_xls.setOnClickListener(this);
        btn_web.setOnClickListener(this);
        btn_muti.setOnClickListener(this);
        btn_signle.setOnClickListener(this);

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
            case R.id.btn_opendoc:
                RxLoad.openFileFromAssets(getApplication(),"testWord.doc");
                break;
            case R.id.btn_openxls:
                RxLoad.openFileFromAssets(getApplication(),"TRD.xls");
                break;
            case R.id.btn_openyouku:
                RxLoad.openFileFromAssets(getApplication(),"testpdf.pdf");
                break;
        }
    }
}
