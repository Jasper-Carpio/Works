package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.primobank.R;
import com.example.primobank.model.Log;
import com.example.primobank.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextBirthday, editTextAddress, editTextEmail, editTextPassword;
    private Button buttonNext;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private ProgressDialog progressDialog;

    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.getSupportActionBar().hide();

        buttonNext = findViewById(R.id.buttonSave);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editPassword);
        progressDialog = new ProgressDialog(this);

        database = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        //show date picker when editTextBirthday click
        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy");
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, i);
                        calendar.set(Calendar.MONTH, i1);
                        calendar.set(Calendar.DAY_OF_MONTH, i2);
                        simpleDateFormat.setTimeZone(calendar.getTimeZone());
                        editTextBirthday.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                //set data picker max data on current date
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUserDetails();
            }
        });
    }

    private void registerUserDetails() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String fullName = editTextFullName.getText().toString();
        String address = editTextAddress.getText().toString();
        String birthday = editTextBirthday.getText().toString();

        if (!email.matches(emailPattern)) {
            editTextEmail.setError("Enter Correct Email");
        } else if (password.isEmpty() || password.length() < 6) {
            editTextPassword.setError("Enter Password Correctly");
        } else {
            progressDialog.setMessage("Registration...");
            progressDialog.setTitle("Register");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            //link email and password on phone number
            firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //create user
                        User user = new User(fullName, address, birthday, firebaseAuth.getCurrentUser().getUid());
                        database.child("users").child(firebaseAuth.getCurrentUser().getPhoneNumber()).setValue(user);

                        //create system log
                        Log log = new Log("User successfully registered information", firebaseAuth.getCurrentUser().getUid());
                        database.child("logs").child(database.child("logs").push().getKey()).setValue(log);

                        Toast.makeText(RegisterActivity.this, "Details successfully registered, please login again", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, SplashScreenActivity.class);
                        startActivity(intent);
                    } else {
                        //create system log
                        Log log = new Log("User failed to register information", firebaseAuth.getCurrentUser().getUid());
                        database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                        Toast.makeText(RegisterActivity.this, "Details failed to register, please try again", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            });
        }
    }
}




