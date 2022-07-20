package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.primobank.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;

public class EnterMobileNumberActivity extends AppCompatActivity {

    private TextView textViewDataPrivacyPolicy;
    private EditText editTextMobileNumber;
    private Button buttonGetOTP;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile_number);
        this.getSupportActionBar().hide();

        //OTP
        editTextMobileNumber = findViewById(R.id.etNumber);
        buttonGetOTP = findViewById(R.id.btnNextEMN);
        progressBar = findViewById(R.id.progressBar);
        textViewDataPrivacyPolicy = findViewById(R.id.textViewDataPrivacyPolicy);

        textViewDataPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DataPrivacyActivity.class);
                startActivity(intent);
            }
        });

        buttonGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextMobileNumber.getText().toString().trim().isEmpty()){
                    Toast.makeText(EnterMobileNumberActivity.this,"Enter Mobile Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                buttonGetOTP.setVisibility(View.INVISIBLE);

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+63" + editTextMobileNumber.getText().toString(), //country code + mobile number
                        60, //timeout in seconds
                        TimeUnit.SECONDS, //cant get new code until the timeout is finished
                        EnterMobileNumberActivity.this,

                    new com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


                         //if verification is sent
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            progressBar.setVisibility(view.GONE);
                            buttonGetOTP.setVisibility(view.VISIBLE);
                        }

                        //if verification is failed or no signal/internet
                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            progressBar.setVisibility(view.GONE);
                            buttonGetOTP.setVisibility(view.VISIBLE);
                            Toast.makeText(EnterMobileNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        //if verification is sent  and ready to verify
                        @Override
                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            progressBar.setVisibility(view.GONE);
                            buttonGetOTP.setVisibility(view.VISIBLE);
                            Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                            intent.putExtra("mobile", editTextMobileNumber.getText().toString());
                            intent.putExtra("verificationId",verificationId);
                            startActivity(intent);
                        }
                    }
                );
            }
        });
    }
}