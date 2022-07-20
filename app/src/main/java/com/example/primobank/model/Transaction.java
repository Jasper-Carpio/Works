package com.example.primobank.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class Transaction {

    public String datetime;
    public String uid;
    public String type;
    public String bankName;
    public String billerName;
    public String phoneNumber;
    public String accountNumber;
    public String accountName;
    public String amount;

    public Transaction() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.getDefault());
        this.datetime = simpleDateFormat.format(new Date());
    }

    public Transaction(String type) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.getDefault());
        if (type.equals("Saving Goal") || type.equals("Withdrawal - Savings Goal")) {
            simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            this.type = type;
        }
        this.datetime = simpleDateFormat.format(new Date());
    }
}
