package com.example.yakov.pomodoro;

public class User {

    public String username;
    public String phone_num;
    public Long current_time_sesion = System.currentTimeMillis();
    public String type_sesion = "work";
    public Boolean run_status = false;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String phone_num) {
        this.username = username;
        this.phone_num = phone_num;
    }

}
// [END blog_user_class]