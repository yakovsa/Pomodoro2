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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;


import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final long START_WORK_IN_MILLIS = 20000;
    private static final long START_SHORT_BREAK_IN_MILLIS = 300000;
    private static final long START_LONG_BREAK_IN_MILLIS = 900000;

    private TextView text_View_timer;
    private TextView break_work;
    private Button startbutton;
    private Button nextbutton;
    private Button Pausebutton;
    private Button Continuebutton;
    private Button Stopbutton;
    private CountDownTimer countDownTimer_timer;
    private int PomodoroNo = 0;
    private boolean is_braek = false;
    private long m_timeleft_insecons = START_WORK_IN_MILLIS;

    private  DatabaseReference ref_in_sesion;
    private  DatabaseReference ref_syn_time;
    private  DatabaseReference ref_syn;
    private boolean status_sesion;

    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_View_timer = findViewById(R.id.text_Timer);
        break_work = findViewById(R.id.break_work);
        startbutton = findViewById(R.id.StartButton);
        Pausebutton = findViewById(R.id.pausebutton);
        Continuebutton = findViewById(R.id.continuebutton);
        Stopbutton = findViewById(R.id.stopbutton);

        ref_syn_time = FirebaseDatabase.getInstance( ).getReference().child("users").child("uri").child("current_time_sesion");
        ref_in_sesion = FirebaseDatabase.getInstance( ).getReference().child("users").child("uri").child("run_status");
        ref_syn = FirebaseDatabase.getInstance( ).getReference().child("users").child("uri");
        mAuth = FirebaseAuth.getInstance();


        startbutton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   writeTimeStart("uri");
                   if(!is_braek)
                   {
                       WorkSession();

                   }
                   startTimer();


               }
           }
        );


        Pausebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer_timer.cancel();
                Continuebutton.setVisibility(View.VISIBLE);
                Pausebutton.setVisibility(View.INVISIBLE);
                Pausebutton.setEnabled(false);
                Continuebutton.setEnabled(true);
            }
        });

        Continuebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
                Continuebutton.setVisibility(View.INVISIBLE);
                Pausebutton.setVisibility(View.VISIBLE);
                Continuebutton.setEnabled(false);
                Pausebutton.setEnabled(true);

            }
        });

        Stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                countDownTimer_timer.cancel();
                ResetAll();

            }
        });


    /*    nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
//                nextbutton.setVisibility(View.INVISIBLE);

            }
        });*/

        updateTimer();
        FirebaseUser currentUser = mAuth.getCurrentUser();

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

                ringtone();
                }
        }.start();
        update_ui2();

    }

    private void ResetAll(){

        if (is_braek) {
            if (PomodoroNo < 4) {
                shortBreak();

            } else {
                longBreak();

            }
        }
        else{
            WorkSession();
        }
    update_ui1();


    }

    private void WorkSession()
    {
        is_braek = false;
        m_timeleft_insecons = START_WORK_IN_MILLIS;
        PomodoroNo++;
        updateTimer();
        break_work.setText("Work session");
        update_ui2();


    }

    private void longBreak() {
        is_braek = true;
        m_timeleft_insecons = START_LONG_BREAK_IN_MILLIS;
        updateTimer();
        PomodoroNo = 0;
        break_work.setText("Long break session");
        update_ui1();


    }

    private void shortBreak() {

        is_braek = true;
        m_timeleft_insecons = START_SHORT_BREAK_IN_MILLIS;
        updateTimer();
        break_work.setText("Short break session");
        update_ui1();
    }

    private void update_ui1()
    {
        Stopbutton.setEnabled(false);
        Stopbutton.setVisibility(View.INVISIBLE);
//        nextbutton.setVisibility(View.VISIBLE);
//        nextbutton.setEnabled(true);
        Pausebutton.setEnabled(false);
        Pausebutton.setVisibility(View.INVISIBLE);
        startbutton.setVisibility(View.VISIBLE);
        startbutton.setEnabled(true);
    }
    private void update_ui2()
    {
        startbutton.setVisibility(View.INVISIBLE);
        Pausebutton.setVisibility(View.VISIBLE);
        Stopbutton.setVisibility(View.VISIBLE);
        Pausebutton.setEnabled(true);
        Stopbutton.setEnabled(true);
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
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }catch (Exception e){
        e.printStackTrace();
    }
       /* try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void writeTimeStart(String userId) {

        long currentTime =  System.currentTimeMillis();
        ref_syn_time.setValue(currentTime);
        ref_in_sesion.setValue(true);

    }

    private void syn_start(String userId) {
        FirebaseUser currentUser = mAuth.getCurrentUser( );
        if (currentUser != null) {
            syn("uri");
        }
    }

    private void syn(String userid)
    {
        // Read from the databas
        ref_syn.addChildEventListener(new ChildEventListener( ) {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                //   Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey( ));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                //   Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey( ));
                if (dataSnapshot.getKey( ).equals("run_status")) {
                    status_sesion = (boolean) dataSnapshot.getValue( );
                    if (!status_sesion) {
                        if (countDownTimer_timer != null) {
                            countDownTimer_timer.cancel( );
                        }

                    }
                }
                if (dataSnapshot.getKey( ).equals("current_time_sesion")) {
                    if (status_sesion) {
                        long start = dataSnapshot.getValue(Long.class);
                        long now = System.currentTimeMillis( );
                        if (countDownTimer_timer != null) {
                            countDownTimer_timer.cancel( );
                        }
                        m_timeleft_insecons = START_WORK_IN_MILLIS - ((now - start) / 1000);
                        startTimer( );
                        startbutton.setVisibility(View.INVISIBLE);
                        nextbutton.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                //   Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey( ));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                // Toast.makeText(mContext, "Failed to load comments.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey( ));

            }
        });

    }

}