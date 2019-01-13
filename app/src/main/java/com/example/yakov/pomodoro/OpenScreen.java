package com.example.yakov.pomodoro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OpenScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(5000);
                        Intent intent = new Intent (OpenScreen.this,MainActivity.class);
                        startActivity( intent);
                        finish();

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
}
