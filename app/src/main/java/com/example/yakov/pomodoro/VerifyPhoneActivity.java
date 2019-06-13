package com.example.yakov.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {


    //These are the objects needed
    //It is the verification id that will be sent to the user
    private String mVerificationId;

    private String mobile;


    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //The edittext to input the code
    private EditText editTextCode;

    //firebase auth object
    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private String userID;

    //to write to database
    //private FirebaseDatabase Users;
    private FirebaseDatabase userSesion;
    private DatabaseReference dataBase;
    private DatabaseReference ref;
    private DatabaseReference Users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        //initializing objects
        mAuth = FirebaseAuth.getInstance();

        mobile = getIntent().getStringExtra("mobile");
        editTextCode = findViewById(R.id.editTextCode);

        Users = FirebaseDatabase.getInstance().getReference("Users");




        //getting mobile number from the previous activity
        //and sending the verification code to the number

        sendVerificationCode(mobile);


       //  writeNewUser(mobile);


        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    editTextCode.setError("Enter valid code");
                    editTextCode.requestFocus();
                    return;
                }

                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

        findViewById(R.id.buttonResend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(mobile, mResendToken);
            }
        });

    }





    //the method is sending verification code

    private void sendVerificationCode(String mobile) {
        //Toast.makeText(getApplicationContext(),mobile,Toast.LENGTH_LONG).show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    private void resendVerificationCode(String mobile,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
/*
            Log.d(null, "onVerificationCompleted:" + credential);

            signInWithPhoneAuthCredential(credential);*/
            //Getting the code sent by SMS
            String code = credential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                editTextCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
            signInWithPhoneAuthCredential(credential);
        }
        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

            super.onCodeSent(verificationId,token);
            Log.d(null, "onCodeSent:" + verificationId);

            //storing the verification id that is sent to the user
            mVerificationId = verificationId;
            mResendToken = token;

        }
    };

    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            FirebaseUser user = task.getResult().getUser();

                            writeNewUser(user.getUid() ,mobile);
                            Intent intent = new Intent(VerifyPhoneActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                                editTextCode.setText(message);
                            }

//                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
//                            snackbar.setAction("Dismiss", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                }
//                            });
//                            snackbar.show();
                        }
                    }
                });
    }

//    private void writeNewUser(String userId, String name, String phone_num) {
//        User user = new User(name, phone_num);
//
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        dataBase = FirebaseDatabase.getInstance().getReference();
//
//        dataBase.child("users").child(userId).setValue(user);
//    }

    private void writeNewUser(String userid ,String phone_num) {

          //String id = Users.push().getKey();
          User user = new User(userid, phone_num);
          Users.child(userid).setValue(user);

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        dataBase = FirebaseDatabase.getInstance().getReference();
//
//        dataBase.child("users").child(userId).setValue(user);
    }


}
