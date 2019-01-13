package com.example.yakov.pomodoro;

public class User {

    public String username;
    public String num_phone;
    public Long current_time_sesion=System.currentTimeMillis();
    public String type_sesion="work";
    public Boolean run_status=false;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String num_phone) {
        this.username = username;
        this.num_phone = num_phone;
    }

}
// [END blog_user_class]