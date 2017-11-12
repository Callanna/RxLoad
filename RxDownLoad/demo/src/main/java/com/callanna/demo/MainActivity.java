package com.callanna.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void toCooker(View view){
        SecondActivity.start(this,SecondActivity.TAG_COOK);
    }
    public void toModel(View view){
        SecondActivity.start(this,SecondActivity.TAG_MODEL);
    }
    public void toOperators(View view){
        SecondActivity.start(this,SecondActivity.TAG_OPERATOR);
    }
}
