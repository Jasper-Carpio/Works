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
import com.example.primobank.model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BuyLoadActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private EditText editTextPhoneNumber;
    private EditText editTextAmount;
    private Button buttonBuyLoad;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private double availableAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_load);
        this.getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        editTextAmount = (EditText) findViewById(R.id.editTextAmount);
        buttonBuyLoad = (Button) findViewById(R.id.buttonBuyLoad);

        progressDialog = new ProgressDialog(BuyLoadActivity.this);

        editTextPhoneNumber.setText(firebaseAuth.getCurrentUser().getPhoneNumber());

        buttonBuyLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = editTextPhoneNumber.getText().toString();
                String amount = editTextAmount.getText().toString();

                if (phoneNumber.isEmpty() || amount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if amount is greater than 1
                if (Double.parseDouble(amount) < 1) {
                    Toast.makeText(BuyLoadActivity.this, "You entered an invalid amount, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if available balance is lower than amount to buy load
                if (Double.parseDouble(amount) > availableAmount) {
                    Toast.makeText(BuyLoadActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if balance will be less than 100
                if ((availableAmount - Double.parseDouble(amount)) <= 100) {
                    Toast.makeText(BuyLoadActivity.this, "You must maintain a balance of 100, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                String recordId;
                Transaction transaction = new Transaction();
                transaction.amount = "-".concat(amount);
                transaction.type = "Buy Load";
                transaction.phoneNumber = phoneNumber;
                transaction.uid = firebaseAuth.getCurrentUser().getUid();

                progressDialog.setMessage("We are processing your buy load request....");
                progressDialog.setCanceledOnTouchOutside(false);
                buttonBuyLoad.setEnabled(false);

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                progressDialog.show();

                //create database record key using UID
                recordId = databaseReference.child("transactions").push().getKey().concat("-").concat(firebaseAuth.getCurrentUser().getUid());

                //create transaction record
                databaseReference.child("transactions").child(recordId).setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        buttonBuyLoad.setEnabled(true);

                        if (!task.isSuccessful()) {
                            //create system log
                            Log log = new Log("User failed to buy load", firebaseAuth.getCurrentUser().getUid());
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(getApplicationContext(), "There is a problem in buy load, please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Buy load successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(BuyLoadActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BuyLoadActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        getAvailableBalance();
    }

    private void getAvailableBalance() {
        progressDialog.setMessage("Checking available balance...");
        progressDialog.setCanceledOnTouchOutside(false);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog.show();

        //get all transactions
        databaseReference.child("transactions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Transaction transaction;
                double amount = 0;

                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get available balance", firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(BuyLoadActivity.this, "Cannot get your available balance, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BuyLoadActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    if(task.getResult().getChildrenCount() > 0) {
                        for (DataSnapshot dataSnapshot: task.getResult().getChildren()) {
                            //check if transaction is from the current user
                            if(!dataSnapshot.getKey().endsWith(firebaseAuth.getCurrentUser().getUid())) {
                                continue;
                            }
                            transaction = dataSnapshot.getValue(Transaction.class);
                            //sums all transactions amount
                            amount += Double.parseDouble(transaction.amount);
                        }

                        availableAmount = amount;

                        //check if available balance is less than 1
                        if (amount < 1) {
                            Toast.makeText(BuyLoadActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(BuyLoadActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                progressDialog.dismiss();
            }
        });
    }
}