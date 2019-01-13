package com.example.yakov.pomodoro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;

public class Open_Screen extends AppCompatActivity {
    CheckBox dont_show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open__screen);
        dont_show = (CheckBox) findViewById(R.id.checkBox);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(5000);
//                        if ()
                        Intent intent = new Intent(Open_Screen.this, AuthActivity.class);
                        startActivity(intent);
                        finish();
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
}
