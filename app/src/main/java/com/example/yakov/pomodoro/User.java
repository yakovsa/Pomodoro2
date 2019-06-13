package com.example.yakov.pomodoro;

import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

public class User  {

    public String username;
    public String phone_num;
    public Long current_time_sesion = System.currentTimeMillis();
    public String type_sesion = "work";
    public Boolean run_status = false;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

//    public User(String username, String phone_num) {
//        this.username = username;
//        this.phone_num = phone_num;
//    }

    public User(String UserId, String phone_num) {

        this.phone_num = phone_num;
    }

}
// [END blog_user_class]