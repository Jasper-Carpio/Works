package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class CashInActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private Spinner spinnerBank;
    private EditText editTextAccountName;
    private EditText editTextAccountNumber;
    private EditText editTextAmount;
    private Button buttonCashIn;
    private Button buttonReceiveThroughQRCode;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;

    private double availableAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_in);
        this.getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(CashInActivity.this);

        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        spinnerBank = (Spinner) findViewById(R.id.spinnerBank);
        editTextAccountName = (EditText) findViewById(R.id.editTextAccountName);
        editTextAccountNumber = (EditText) findViewById(R.id.editTextAccountNumber);
        editTextAmount = (EditText) findViewById(R.id.editTextAmount);
        buttonCashIn = (Button) findViewById(R.id.buttonCashIn);
        buttonReceiveThroughQRCode = (Button) findViewById(R.id.buttonReceivedThroughQRCode);

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CashInActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        buttonCashIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Transaction transaction;
                String bankName = spinnerBank.getSelectedItem().toString();
                String accountName = editTextAccountName.getText().toString();
                String accountNumber = editTextAccountNumber.getText().toString();
                String amount = editTextAmount.getText().toString();

                if(accountName.isEmpty() || accountNumber.isEmpty() || amount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if amount is greater than 1
                if (Double.parseDouble(amount) < 1) {
                    Toast.makeText(CashInActivity.this, "You entered an invalid amount, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if amount is greater than account limit
                if (availableAmount > 500000 || (availableAmount+ Double.parseDouble(amount)) > 500000) {
                    Toast.makeText(CashInActivity.this, "You're account limit is only up to PHP500,000.00, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                transaction = new Transaction();
                transaction.uid = firebaseAuth.getCurrentUser().getUid();
                transaction.type = "Cash In";
                transaction.accountName = accountName;
                transaction.accountNumber = accountNumber;
                transaction.bankName = bankName;
                transaction.amount = amount;

                AlertDialog.Builder builder = new AlertDialog.Builder(CashInActivity.this);
                builder.setMessage("Please confirm to proceed with the cash in request");
                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveTransaction(transaction);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        buttonReceiveThroughQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CashInActivity.this, DashboardActivity.class);
                intent.putExtra("setBottomNavigationActive", "QR");
                startActivity(intent);
            }
        });

        getAvailableBalance();
    }

    private void saveTransaction(Transaction transaction) {
        String recordId;
        progressDialog.setMessage("We are processing your cash in request....");
        progressDialog.setCanceledOnTouchOutside(false);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog.show();
        buttonCashIn.setEnabled(false);

        //create database record key using UID
        recordId = database.child("transactions").push().getKey().concat("-").concat(firebaseAuth.getCurrentUser().getUid());

        //create transaction record
        database.child("transactions").child(recordId).setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to cash in", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getApplicationContext(), "There is a problem in cash in, please try again", Toast.LENGTH_SHORT).show();
                    buttonCashIn.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Cash In successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CashInActivity.this, DashboardActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void getAvailableBalance() {
        progressDialog.setMessage("Checking available balance...");
        progressDialog.setCanceledOnTouchOutside(false);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog.show();

        //get all transactions
        database.child("transactions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Transaction transaction;
                double amount = 0;

                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get available balance", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(CashInActivity.this, "Cannot get your available balance, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CashInActivity.this, DashboardActivity.class);
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
                    }
                }
                progressDialog.dismiss();
            }
        });
    }
}