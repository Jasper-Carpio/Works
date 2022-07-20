package com.example.primobank.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SavingGoalActivity extends AppCompatActivity {

    private EditText editTextAmount;
    private EditText editTextTargetAmount;
    private TextView textViewTotalSavings;
    private ImageView imageViewBack;
    private ProgressDialog progressDialog;
    private ValueLineChart valueLineChart;
    private Button buttonAddSaving;
    private Button buttonWithdrawSavings;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private List<Transaction> transactions;
    private ValueLineSeries valueLineSeries;
    private double availableAmount;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_goal);
        this.getSupportActionBar().hide();

        editTextAmount = (EditText) findViewById(R.id.editTextAmount);
        editTextTargetAmount = (EditText) findViewById(R.id.editTextTargetAmount);
        textViewTotalSavings = (TextView) findViewById(R.id.textViewTotalSavings);
        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        valueLineChart = (ValueLineChart) findViewById(R.id.valueLineChart);
        buttonAddSaving = (Button) findViewById(R.id.buttonAddSaving);
        buttonWithdrawSavings = (Button) findViewById(R.id.buttonWithdrawSavings);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(SavingGoalActivity.this);

        editTextTargetAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //update chart target amount if value change
                if (valueLineSeries != null) {
                    valueLineChart.clearChart();
                    valueLineChart.addSeries(valueLineSeries);
                }
                setTargetAmountOnChart();
                saveTargetAmount();
            }
        });

        buttonWithdrawSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double totalSavings = Double.parseDouble(textViewTotalSavings.getText().toString());
                //check if total savings is lower than 1
                if (totalSavings < 1) {
                    Toast.makeText(SavingGoalActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                String recordId;
                Transaction transaction = new Transaction("Withdrawal - Savings Goal");
                transaction.amount = String.valueOf(totalSavings);
                transaction.uid = firebaseAuth.getCurrentUser().getUid();

                progressDialog.setMessage("We are processing your withdrawal request....");
                progressDialog.setCanceledOnTouchOutside(false);
                buttonWithdrawSavings.setEnabled(false);

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
                        buttonWithdrawSavings.setEnabled(true);

                        if (!task.isSuccessful()) {
                            //create system log
                            Log log = new Log("User failed to withdraw savings", firebaseAuth.getCurrentUser().getUid());
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(getApplicationContext(), "There is a problem in withdrawing your savings, please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Withdraw saving successful", Toast.LENGTH_SHORT).show();
                            setSavingGoalTransactions();
                        }
                    }
                });

            }
        });

        buttonAddSaving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = editTextAmount.getText().toString();

                if (amount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill amount fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if available balance is lower than amount to save
                if (Double.parseDouble(amount) > availableAmount) {
                    Toast.makeText(SavingGoalActivity.this, "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if balance will be less than 100
                if ((availableAmount - Double.parseDouble(amount)) <= 100) {
                    Toast.makeText(SavingGoalActivity.this, "You must maintain a balance of 100, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                String recordId;
                Transaction transaction = new Transaction("Saving Goal");
                transaction.amount = "-".concat(amount);
                transaction.uid = firebaseAuth.getCurrentUser().getUid();

                progressDialog.setMessage("We are processing your saving request....");
                progressDialog.setCanceledOnTouchOutside(false);
                buttonAddSaving.setEnabled(false);

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
                        buttonAddSaving.setEnabled(true);

                        if (!task.isSuccessful()) {
                            //create system log
                            Log log = new Log("User failed to add saving", firebaseAuth.getCurrentUser().getUid());
                            databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                            Toast.makeText(getApplicationContext(), "There is a problem in adding saving, please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Add saving successful", Toast.LENGTH_SHORT).show();
                            setSavingGoalTransactions();
                        }
                    }
                });
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavingGoalActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        transactions = new ArrayList<Transaction>();
        setTargetAmount();
        setSavingGoalTransactions();
    }

    //get saved target amount on firebase and set to edit text
    private void saveTargetAmount() {
        user.savingsGoalTargetAmount = editTextTargetAmount.getText().toString();
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getPhoneNumber()).setValue(user);
    }

    private void setSavingGoalTransactions() {
        transactions.clear();
        progressDialog.setMessage("Getting saving goal transactions...");
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
                    Log log = new Log("User failed to get all transactions", firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(SavingGoalActivity.this, "Cannot get your transactions, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SavingGoalActivity.this, DashboardActivity.class);
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

                            if(transaction.type.equals("Saving Goal") || transaction.type.equals("Withdrawal - Savings Goal")) {
                                transactions.add(transaction);
                            }
                        }
                    }

                    availableAmount = amount;
                }
                progressDialog.dismiss();
                setChart();
            }
        });
    }

    private void setChart() {
        valueLineSeries = new ValueLineSeries();
        List<String> daysOfTheWeek = getDaysOftheWeek();
        HashMap<String,Double> dailySavings =new HashMap<String,Double>();
        double amount = 0;
        valueLineSeries.setColor(getResources().getColor(R.color.activity_background));

        if(transactions.isEmpty()) {
            for (String day: daysOfTheWeek) {
                valueLineSeries.addPoint(new ValueLinePoint(day, 0));
            }
        } else {
            for (String day: daysOfTheWeek) {
                for (Transaction transaction: transactions) {
                    if (transaction.datetime.startsWith(day)) {
                        if (transaction.type.equals("Saving Goal")) {
                            amount += Math.abs(Double.parseDouble(transaction.amount));
                        } else if (transaction.type.equals("Withdrawal - Savings Goal")) {
                            amount -= Math.abs(Double.parseDouble(transaction.amount));
                        }
                    }
                }
                valueLineSeries.addPoint(new ValueLinePoint(day, Float.parseFloat(String.valueOf(amount))));
            }
        }

        textViewTotalSavings.setText(String.valueOf(amount));
        valueLineChart.clearChart();
        valueLineChart.addSeries(valueLineSeries);

        setTargetAmountOnChart();

        valueLineChart.setUseCubic(true);
        valueLineChart.startAnimation();
    }

    private void setTargetAmount() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get profile information", firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(SavingGoalActivity.this, "There is a problem getting the profile information, please try again", Toast.LENGTH_SHORT).show();
                } else {
                    //get data from user record
                    user = task.getResult().getValue(User.class);
                    editTextTargetAmount.setText(user.savingsGoalTargetAmount);
                }
            }
        });
    }

    private void setTargetAmountOnChart() {
        ValueLineSeries series;
        List<String> daysOfTheWeek = getDaysOftheWeek();

        if (!editTextTargetAmount.getText().toString().isEmpty()) {
            series = new ValueLineSeries();
            series.setColor(getResources().getColor(R.color.button_background));
            for (String day: daysOfTheWeek) {
                series.addPoint(new ValueLinePoint(day, Float.parseFloat(editTextTargetAmount.getText().toString())));
            }
            valueLineChart.addSeries(series);
        }
    }

    private List<String> getDaysOftheWeek() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
        List<String> daysOfTheWeek = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();

        simpleDateFormat.setTimeZone(calendar.getTimeZone());
        calendar.setTime(new Date());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 7);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        daysOfTheWeek.add(simpleDateFormat.format(calendar.getTime()));

        return daysOfTheWeek;
    }
}