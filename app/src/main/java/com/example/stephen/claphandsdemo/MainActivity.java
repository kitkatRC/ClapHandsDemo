package com.example.stephen.claphandsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private ClapHandProgressView mPrb;
    private int mCurrent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPrb = findViewById(R.id.chp);
        mPrb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrb.setMax(1000);
                mCurrent++;
                mPrb.setProgress(mCurrent);
            }
        });
    }
}
