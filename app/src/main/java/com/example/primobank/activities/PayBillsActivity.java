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
import android.widget.Spinner;
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

public class PayBillsActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private Spinner spinnerBill;
    private EditText editTextAccountName;
    private EditText editTextAccountNumber;
    private EditText editTextAmount;
    private ProgressDialog progressDialog;
    private Button buttonPayBill;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private double availableAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bills);
        this.getSupportActionBar().hide();

        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        spinnerBill = (Spinner) findViewById(R.id.spinnerBill);
        editTextAccountName = (EditText) findViewById(R.id.editTextAccountName);
        editTextAccountNumber = (EditText) findViewById(R.id.editTextAccountNumber);
        editTextAmount = (EditText) findViewById(R.id.editTextAmount);
        buttonPayBill = (Button) findViewById(R.id.buttonPayBill);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(PayBillsActivity.this);

        buttonPayBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Transaction transaction;
                String recordId;
                String billerName = spinnerBill.getSelectedItem().toString();
                String accountName = editTextAccountName.getText().toString();
                String accountNumber = editTextAccountNumber.getText().toString();
                String amount = editTextAmount.getText().toString();

                if(accountName.isEmpty() || accountNumber.isEmpty() || amount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if amount is greater than 1
                if (Double.parseDouble(amount) < 1) {
                    Toast.makeText(PayBillsActivity.this, "You entered an invalid amount, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if available balance is lower than amount to pay bills
                if (Double.parseDouble(amount) > availableAmount) {
                    Toast.makeText(PayBillsActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if balance will be less than 100
                if ((availableAmount - Double.parseDouble(amount)) <= 100) {
                    Toast.makeText(PayBillsActivity.this, "You must maintain a balance of 100, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                transaction = new Transaction();
                transaction.uid = firebaseAuth.getCurrentUser().getUid();
                transaction.type = "Pay Bills";
                transaction.accountName = accountName;
                transaction.accountNumber = accountNumber;
                transaction.billerName = billerName;
                transaction.amount = "-" + amount;

                progressDialog.setMessage("We are processing your pay bill request....");
                progressDialog.setCanceledOnTouchOutside(false);

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                progressDialog.show();
                buttonPayBill.setEnabled(false);

                //create database record key using UID
                recordId = databaseReference.child("transactions").push().getKey().concat("-").concat(firebaseAuth.getCurrentUser().getUid());

                //create transaction record
                databaseReference.child("transactions").child(recordId).setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            //create system log
                            Log log = new Log("User failed to pay bill", firebaseAuth.getCurrentUser().getUid());
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(getApplicationContext(), "There is a problem in pay bill, please try again", Toast.LENGTH_SHORT).show();
                            buttonPayBill.setEnabled(true);
                        } else {
                            Toast.makeText(getApplicationContext(), "Pay bill successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PayBillsActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });


        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PayBillsActivity.this, DashboardActivity.class);
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
                    Toast.makeText(PayBillsActivity.this, "Cannot get your available balance, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PayBillsActivity.this, DashboardActivity.class);
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
                            Toast.makeText(PayBillsActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PayBillsActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                progressDialog.dismiss();
            }
        });
    }

}