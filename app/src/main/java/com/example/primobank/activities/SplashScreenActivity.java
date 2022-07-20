package com.example.primobank.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.primobank.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        this.getSupportActionBar().hide();

        FirebaseAuth.getInstance().signOut();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }, 5000);
    }
}

