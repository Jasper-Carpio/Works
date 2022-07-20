package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.primobank.R;
import com.example.primobank.model.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogInActivity extends AppCompatActivity {

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private ProgressDialog progressDialog;
    private TextView textViewSignUp;
    private TextView textViewForgotPassword;
    private Button buttonLogin;
    private EditText editTextEmail, editTextPassword;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        this.getSupportActionBar().hide();

        textViewSignUp = findViewById(R.id.tvSignup);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        buttonLogin = findViewById(R.id.btnLoginLog);
        editTextEmail = findViewById(R.id.etEmailLog);
        editTextPassword = findViewById(R.id.etPasswordLog);

        database = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        progressDialog = new ProgressDialog(this);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            performLogin();
            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, EnterMobileNumberActivity.class);
                startActivity(intent);
            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (!email.matches(emailPattern)) {
            editTextEmail.setError("Enter Correct Email");
        } else if (password.isEmpty() || password.length() < 6) {
            editTextPassword.setError("Enter Password Correctly");
        } else {
            progressDialog.setMessage("Please Wait While Logging In...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        firebaseUser = task.getResult().getUser();
                        //create system log
                        Log log = new Log("User successfully logged-in", firebaseUser.getUid());
                        database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                        Toast.makeText(LogInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LogInActivity.this, DashboardActivity.class);
                        startActivity(intent);
                    } else {
                        //create system log
                        Log log = new Log("User failed to log-in", email);
                        database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                        Toast.makeText(LogInActivity.this, " " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}