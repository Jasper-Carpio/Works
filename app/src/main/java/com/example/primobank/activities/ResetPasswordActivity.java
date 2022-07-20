package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.primobank.R;
import com.example.primobank.model.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private EditText editTextEmail;
    private Button buttonResetPassword;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        this.getSupportActionBar().hide();

        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        buttonResetPassword = (Button) findViewById(R.id.buttonResetPassword);

        progressDialog = new ProgressDialog(ResetPasswordActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();

                if(email.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill the field", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setMessage("Sending Reset Password to User Email...");
                progressDialog.setCanceledOnTouchOutside(false);

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                progressDialog.show();
                buttonResetPassword.setEnabled(false);

                //send password reset to user email address
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //create system log
                            Log log = new Log("User successfully requested to reset password", email);
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(ResetPasswordActivity.this, "Please check your email to reset password", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPasswordActivity.this, SplashScreenActivity.class);
                            startActivity(intent);
                        } else {
                            //create system log
                            Log log = new Log("User failed to request to reset password", email);
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(ResetPasswordActivity.this, "Failed to send password, please try again", Toast.LENGTH_SHORT).show();
                        }
                        buttonResetPassword.setEnabled(true);
                        progressDialog.dismiss();
                    }
                });
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResetPasswordActivity.this, SplashScreenActivity.class);
                startActivity(intent);
            }
        });
    }
}