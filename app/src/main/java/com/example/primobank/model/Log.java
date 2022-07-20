package com.example.primobank.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class Log {

    public String description;
    public String datetime;
    public String uid;

    public Log() {}

    public Log(String description, String datetime, String uid) {
        this.description = description;
        this.datetime = datetime;
        this.uid = uid;
    }

    public Log(String description, String uid) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.getDefault());
        this.description = description;
        this.datetime = simpleDateFormat.format(new Date());
        this.uid = uid;
    }
}
