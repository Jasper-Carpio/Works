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
import com.example.primobank.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendMoneyActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private Button buttonScanQRCode;
    private Button buttonSendMoney;
    private Button buttonBankTransfer;
    private EditText editTextPhoneNumber;
    private EditText editTextAmount;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private double availableAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);
        this.getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        buttonScanQRCode = (Button) findViewById(R.id.buttonScanQRCode);
        buttonSendMoney = (Button) findViewById(R.id.buttonSendMoney);
        buttonBankTransfer = (Button) findViewById(R.id.buttonBankTransfer);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        editTextAmount = (EditText) findViewById(R.id.editTextAmount);

        progressDialog = new ProgressDialog(SendMoneyActivity.this);

        //use phone number if from QR code scanner activity
        if(getIntent().getStringExtra("phoneNumber") != null) {
            editTextPhoneNumber.setText(getIntent().getStringExtra("phoneNumber"));
        }

        buttonScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendMoneyActivity.this, QRCodeScannerActivity.class);
                startActivity(intent);
            }
        });

        buttonBankTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendMoneyActivity.this, BankTransferActivity.class);
                startActivity(intent);
            }
        });

        buttonSendMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = editTextPhoneNumber.getText().toString();
                String amount = editTextAmount.getText().toString();

                if (phoneNumber.isEmpty() || amount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if phone number is from the same current user
                if (firebaseAuth.getCurrentUser().getPhoneNumber().endsWith(phoneNumber)) {
                    Toast.makeText(getApplicationContext(), "Sending money to self cannot be processed, please use other number", Toast.LENGTH_SHORT).show();
                    editTextPhoneNumber.setText("");
                    return;
                }

                //check if amount is greater than 1
                if (Double.parseDouble(amount) < 1) {
                    Toast.makeText(SendMoneyActivity.this, "You entered an invalid amount, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if available balance is lower than amount to send money
                if (Double.parseDouble(amount) > availableAmount) {
                    Toast.makeText(SendMoneyActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if balance will be less than 100
                if ((availableAmount - Double.parseDouble(amount)) <= 100) {
                    Toast.makeText(SendMoneyActivity.this, "You must maintain a balance of 100, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //get all users
                databaseReference.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        User user = null;
                        if(!task.isSuccessful()) {
                            //create system log
                            Log log = new Log("There is issue on getting phone numbers" + phoneNumber, firebaseAuth.getCurrentUser().getUid());
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(getApplicationContext(), "There is a problem on the phone number, please use another number", Toast.LENGTH_SHORT).show();
                        } else {
                            for (DataSnapshot dataSnapshot: task.getResult().getChildren()) {
                                //check if phone number is from existing user
                                if (dataSnapshot.getKey().endsWith(phoneNumber)) {
                                    user = dataSnapshot.getValue(User.class);
                                }
                            }

                            if (user == null) {
                                Toast.makeText(getApplicationContext(), "There is a problem on the phone number, please use another number", Toast.LENGTH_SHORT).show();
                            } else {
                                sendMoney(phoneNumber, user.uid, amount);
                            }
                        }
                    }
                });
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendMoneyActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        getAvailableBalance();
    }

    private void sendMoney(String phoneNumber, String uid, String amount) {
        String recordId;
        Transaction transaction = new Transaction();
        transaction.amount = "-".concat(amount);
        transaction.type = "Send Money";
        transaction.phoneNumber = phoneNumber;
        transaction.uid = firebaseAuth.getCurrentUser().getUid();

        progressDialog.setMessage("We are processing your send money request....");
        progressDialog.setCanceledOnTouchOutside(false);
        buttonSendMoney.setEnabled(false);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        //create database record key using UID
        recordId = databaseReference.child("transactions").push().getKey().concat("-").concat(firebaseAuth.getCurrentUser().getUid());

        //create transaction record
        databaseReference.child("transactions").child(recordId).setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                buttonSendMoney.setEnabled(true);

                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to send money", firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getApplicationContext(), "There is a problem in send money, please try again", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Send money successful", Toast.LENGTH_SHORT).show();
                    generateTransactionForReceiver(transaction, amount, uid);
                    Intent intent = new Intent(SendMoneyActivity.this, DashboardActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    //create transaction record for the receiver of money
    private void generateTransactionForReceiver(Transaction transaction, String amount, String uid) {
        String recordId = databaseReference.child("transactions").push().getKey().concat("-").concat(uid);
        Transaction newTransaction = new Transaction();
        newTransaction.type = "Receive Money";
        newTransaction.amount = amount;
        newTransaction.uid = transaction.uid;
        databaseReference.child("transactions").child(recordId).setValue(newTransaction);
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
                    Toast.makeText(SendMoneyActivity.this, "Cannot get your available balance, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SendMoneyActivity.this, DashboardActivity.class);
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
                            Toast.makeText(SendMoneyActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SendMoneyActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                progressDialog.dismiss();
            }
        });
    }
}