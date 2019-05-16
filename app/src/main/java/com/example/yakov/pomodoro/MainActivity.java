package com.example.yakov.pomodoro;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;


import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final long START_WORK_IN_MILLIS = 1500500;
    private static final long START_SHORT_BREAK_IN_MILLIS = 300000;
    private static final long START_LONG_BREAK_IN_MILLIS = 900000;

    private TextView text_View_timer;
    private TextView break_work;
    private Button startbutton;
    private Button nextbutton;
    private CountDownTimer countDownTimer_timer;
    private int PomodoroNo = 0;
    private boolean is_braek = false;
    private long m_timeleft_insecons = START_WORK_IN_MILLIS;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_View_timer = findViewById(R.id.text_Timer);
        break_work = findViewById(R.id.break_work);
        startbutton = findViewById(R.id.StartButton);
        nextbutton = findViewById(R.id.nextbutton);
//        nextbutton.setVisibility(View.INVISIBLE);

        startbutton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(!is_braek)
                   {
                       WorkSession();

                   }
                   startTimer();
                   startbutton.setVisibility(View.INVISIBLE);
                   nextbutton.setVisibility(View.VISIBLE);

               }
           }
        );
        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer_timer.cancel();
                if(!is_braek){
                    if (PomodoroNo < 4) {
                        shortBreak();

                    } else {
                        longBreak();
                    }
                }
                else {
                    WorkSession();


                }
                startbutton.setVisibility(View.VISIBLE);
                nextbutton.setVisibility(View.INVISIBLE);

            }
        });

        updateTimer();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case  R.id.statistics:
                Intent intent1 = new Intent(this,Statistics.class);
                this.startActivity(intent1);
                return true;

            case  R.id.about:
                Intent intent2 = new Intent(this,About.class);
                this.startActivity(intent2);
                return true;

            case  R.id.signout:
                Intent intent3 = new Intent(this,SignOut.class);
                this.startActivity(intent3);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startTimer() {
        countDownTimer_timer = new CountDownTimer(m_timeleft_insecons, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                m_timeleft_insecons = millisUntilFinished;
                updateTimer();

            }

            @Override
            public void onFinish() {
                if (!is_braek) {
                    if (PomodoroNo < 4) {
                        shortBreak();

                    } else {
                        longBreak();

                    }
                }
                else{
                    WorkSession();
                }
                startbutton.setVisibility(View.VISIBLE);
                nextbutton.setVisibility(View.INVISIBLE);
                ringtone();
                }
        }.start();
        nextbutton.setVisibility(View.INVISIBLE);

    }

    private void WorkSession()
    {
        is_braek = false;
        m_timeleft_insecons = START_WORK_IN_MILLIS;
        PomodoroNo++;
        updateTimer();
        break_work.setText("Work session");

    }

    private void longBreak() {
        is_braek = true;
        m_timeleft_insecons = START_LONG_BREAK_IN_MILLIS;
        updateTimer();
        PomodoroNo = 0;
        break_work.setText("Long break session");

    }

    private void shortBreak() {

        is_braek = true;
        m_timeleft_insecons = START_SHORT_BREAK_IN_MILLIS;
        updateTimer();
        break_work.setText("Short break session");
    }


    private void updateTimer() {
        int minutes = (int) (m_timeleft_insecons / 1000) / 60;
        int seconds = (int) ((m_timeleft_insecons /1000) % 60);

        String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        text_View_timer.setText(timeLeft);

    }
    public void ringtone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}