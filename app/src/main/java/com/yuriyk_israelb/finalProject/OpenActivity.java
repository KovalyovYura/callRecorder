package com.yuriyk_israelb.finalProject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class OpenActivity extends AppCompatActivity {
    private boolean isFirst = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        isFirst = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                toMainActivity();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isFirst){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(2000);
                    toMainActivity();
                }
            }).start();
        }
    }

    private void toMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
