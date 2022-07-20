package com.example.primobank.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String fullName;
    public String address;
    public String birthday;
    public String uid;
    public String savingsGoalTargetAmount;

    public User() {}

    public User(String fullName, String address, String birthday, String uid) {
        this.fullName = fullName;
        this.address = address;
        this.birthday = birthday;
        this.uid = uid;
        this.savingsGoalTargetAmount = "";
    }
}
