package com.example.primobank.fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.primobank.R;
import com.example.primobank.activities.SplashScreenActivity;
import com.example.primobank.model.Log;
import com.example.primobank.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DashboardProfileFragment extends Fragment {

    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private EditText editTextFullName;
    private EditText editTextBirthday;
    private EditText editTextAddress;
    private Button buttonSave;
    private Button buttonResetPassword;
    private Button buttonLogout;
    private ProgressDialog progressDialog;

    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;

    private User user;

    public DashboardProfileFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        editTextEmail = (EditText) view.findViewById(R.id.editTextEmail);
        editTextPhoneNumber = (EditText) view.findViewById(R.id.editTextPhoneNumber);
        editTextFullName = (EditText) view.findViewById(R.id.editTextFullName);
        editTextBirthday = (EditText) view.findViewById(R.id.editTextBirthday);
        editTextAddress = (EditText) view.findViewById(R.id.editTextAddress);
        buttonSave = (Button) view.findViewById(R.id.buttonSave);
        buttonResetPassword = (Button) view.findViewById(R.id.buttonResetPassword) ;
        buttonLogout = (Button) view.findViewById(R.id.buttonLogout);

        //show date picker when editTextBirthday click
        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
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

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonSave.setEnabled(false);
                if (buttonSave.getText().toString() == "SAVE") {
                    saveUserInformation();
                } else if (buttonSave.getText().toString() == "GET PROFILE INFORMATION") {
                    getUserInformation();
                }
            }
        });

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Sending Reset Password to User Email...");
                progressDialog.setCanceledOnTouchOutside(false);

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                progressDialog.show();
                buttonResetPassword.setEnabled(false);

                //send password reset to user email address
                firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //create system log
                                Log log = new Log("User successfully requested to reset password", firebaseAuth.getCurrentUser().getUid());
                                database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                                Toast.makeText(getActivity(), "Please check your email to reset password", Toast.LENGTH_SHORT).show();
                            } else {
                                //create system log
                                Log log = new Log("User failed to request to reset password", firebaseAuth.getCurrentUser().getUid());
                                database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                                Toast.makeText(getActivity(), "Failed to send password, please try again", Toast.LENGTH_SHORT).show();
                            }
                            buttonResetPassword.setEnabled(true);
                            progressDialog.dismiss();
                        }
                    });
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create system log
                Log log = new Log("User logged-out", firebaseAuth.getCurrentUser().getUid());
                database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
                startActivity(intent);
            }
        });

        progressDialog = new ProgressDialog(getActivity());
        editTextEmail.setText(firebaseAuth.getCurrentUser().getEmail());
        editTextPhoneNumber.setText(firebaseAuth.getCurrentUser().getPhoneNumber());
        getUserInformation();
        return view;
    }

    private void getUserInformation() {
        progressDialog.setMessage("Getting Profile Information...");
        progressDialog.setCanceledOnTouchOutside(false);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog.show();
        buttonSave.setEnabled(false);

        database.child("users").child(firebaseAuth.getCurrentUser().getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get profile information", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getActivity(), "There is a problem getting the profile information, please try again", Toast.LENGTH_SHORT).show();
                    buttonSave.setText("GET PROFILE INFORMATION");
                } else {
                    //get data from user record
                    user = task.getResult().getValue(User.class);
                    editTextAddress.setText(user.address);
                    editTextBirthday.setText(user.birthday);
                    editTextFullName.setText(user.fullName);
                    buttonSave.setText("SAVE");
                }
                buttonSave.setEnabled(true);
                progressDialog.dismiss();
            }
        });
    }

    private void saveUserInformation() {
        progressDialog.setMessage("Saving Profile Information...");
        progressDialog.setCanceledOnTouchOutside(false);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog.show();
        buttonSave.setEnabled(false);

        String fullName = editTextFullName.getText().toString();
        String address = editTextAddress.getText().toString();
        String birthday = editTextBirthday.getText().toString();

        User updatedUser = new User(fullName, address, birthday, firebaseAuth.getCurrentUser().getUid());
        updatedUser.savingsGoalTargetAmount = user.savingsGoalTargetAmount;

        //update user information
        database.child("users").child(firebaseAuth.getCurrentUser().getPhoneNumber()).setValue(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to update information", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getActivity(), "There is a problem saving the profile information, please try again", Toast.LENGTH_SHORT).show();
                } else {
                    //create system log
                    Log log = new Log("User successfully updated information", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getActivity(), "Profile successfully saved", Toast.LENGTH_SHORT).show();
                }
                buttonSave.setEnabled(true);
                progressDialog.dismiss();
            }
        });
    }
}