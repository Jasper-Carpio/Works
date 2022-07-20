package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.primobank.R;
import com.example.primobank.fragments.DashboardHomeFragment;
import com.example.primobank.fragments.DashboardProfileFragment;
import com.example.primobank.fragments.DashboardQRFragment;
import com.example.primobank.fragments.DashboardTransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {

    private BottomNavigationView bottomNavigationView;
    private DashboardHomeFragment dashboardHomeFragment;
    private DashboardQRFragment dashboardQRFragment;
    private DashboardTransactionsFragment dashboardTransactionsFragment;
    private DashboardProfileFragment dashboardProfileFragment;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        this.getSupportActionBar().hide();

        dashboardHomeFragment = new DashboardHomeFragment();
        dashboardProfileFragment = new DashboardProfileFragment();
        dashboardQRFragment = new DashboardQRFragment();
        dashboardTransactionsFragment = new DashboardTransactionsFragment();

        firebaseAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        if(getIntent().getStringExtra("setBottomNavigationActive") != null) {
            if(getIntent().getStringExtra("setBottomNavigationActive").equals("QR")) {
                bottomNavigationView.setSelectedItemId(R.id.qr);
            } else {
                bottomNavigationView.setSelectedItemId(R.id.home);
            }
        } else {
            bottomNavigationView.setSelectedItemId(R.id.home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, dashboardHomeFragment).commit();
                return true;
            case R.id.qr:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, dashboardQRFragment).commit();
                return true;
            case R.id.transactions:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, dashboardTransactionsFragment).commit();
                return true;
            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, dashboardProfileFragment).commit();
                return true;
        }
        return false;
    }
}