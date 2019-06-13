package com.example.yakov.pomodoro;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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


    private FirebaseUser user;
    private  DatabaseReference Usres;
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
        Usres = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


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




        updateTimer();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Usres.child(currentUser.getUid()).child("Token").setValue(token);

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

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
                showAlertDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
//========================================================================================================
//                                    Dialog to SignOut
//========================================================================================================
    public void showAlertDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("confirm Sign out");

        alertDialog.setMessage("Are you sure you want to Sign out?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignOut();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();

    }
//                                          SignOut the User
    private void SignOut()
    {
        DatabaseReference dr_user = Usres.child("+972535305754");
        dr_user.removeValue();

        mAuth.signOut();
    }
//==================================================================================================
//                                       manage the timer
//==================================================================================================
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
    }

    private void writeTimeStart(String userId) {

        long currentTime =  System.currentTimeMillis();
        ref_syn_time.setValue(currentTime);
        ref_in_sesion.setValue(true);

    }

    private void syn_start(String userId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //syn("uri");
        }
    }

    private void syn(String userid)
    {
        // Read from the databas
        ref_syn.addChildEventListener(new ChildEventListener( ) {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                   Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey( ));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                   Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey( ));
                if (dataSnapshot.getKey().equals("run_status")) {
                    status_sesion = (boolean) dataSnapshot.getValue( );
                    if (!status_sesion) {
                        if (countDownTimer_timer != null) {
                            countDownTimer_timer.cancel( );
                        }

                    }
                }
                if (dataSnapshot.getKey().equals("current_time_sesion")) {
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
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                 //Toast.makeText(mContext, "Failed to load comments.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                 Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey( ));

            }
        });

    }

    //----------------------------------------------------------------------------------------------------------------


}