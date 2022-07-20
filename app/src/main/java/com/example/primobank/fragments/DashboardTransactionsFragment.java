package com.example.primobank.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.primobank.R;
import com.example.primobank.RecylclerViewTransactionsAdapter;
import com.example.primobank.activities.DashboardActivity;
import com.example.primobank.activities.PayBillsActivity;
import com.example.primobank.model.Log;
import com.example.primobank.model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DashboardTransactionsFragment extends Fragment {

    private FrameLayout frameLayout;
    private RecyclerView recyclerViewTransactions;
    private TextView textViewResults;
    private EditText editTextStartDate;
    private Button buttonResetStartDate;
    private Spinner spinnerFilterBy;

    private List<Transaction> transactions;

    private RecylclerViewTransactionsAdapter recylclerViewTransactionsAdapter;

    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;

    private Date filterDate;

    public DashboardTransactionsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_transactions, container, false);

        frameLayout = (FrameLayout) view.findViewById(R.id.frameLayout);
        recyclerViewTransactions = (RecyclerView) view.findViewById(R.id.recyclerViewTransactions);
        textViewResults = (TextView) view.findViewById(R.id.textViewResults);
        editTextStartDate = (EditText) view.findViewById(R.id.editTextStartDate);
        buttonResetStartDate = (Button) view.findViewById(R.id.buttonResetStartDate);
        spinnerFilterBy = (Spinner) view.findViewById(R.id.spinnerFilterBy);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        spinnerFilterBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String transactionType = spinnerFilterBy.getSelectedItem().toString();

                if (transactionType.equals("All")) {
                    getTransactions();
                } else {
                    getTransactionsFilterBy(transactionType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        buttonResetStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextStartDate.setText("");
                filterDate = null;
                getTransactions();
            }
        });

        //show date picker when editTextBirthday click
        editTextStartDate.setOnClickListener(new View.OnClickListener() {
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
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        simpleDateFormat.setTimeZone(calendar.getTimeZone());
                        editTextStartDate.setText(simpleDateFormat.format(calendar.getTime()));
                        filterDate = calendar.getTime();
                        getTransactions();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                //set data picker max data on current date
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        transactions = new ArrayList<Transaction>();

        recylclerViewTransactionsAdapter = new RecylclerViewTransactionsAdapter(getActivity(), transactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewTransactions.setAdapter(recylclerViewTransactionsAdapter);

        getTransactions();

        return view;
    }

    private void getTransactions() {
        frameLayout.setVisibility(View.GONE);
        textViewResults.setVisibility(View.VISIBLE);
        textViewResults.setText("Loading transactions...");

        //get all transactions
        database.child("transactions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm");
                SimpleDateFormat simpleDateFormatSavingsGoal = new SimpleDateFormat("MMMM d, yyyy");
                Transaction transaction;
                Date date;
                Calendar calendar;
                List<Transaction> newTransactions = new ArrayList<Transaction>();

                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get transactions", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getActivity(), "Cannot get your transactions, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), DashboardActivity.class);
                    startActivity(intent);
                } else {
                    if(task.getResult().getChildrenCount() > 0) {
                        for (DataSnapshot dataSnapshot: task.getResult().getChildren()) {
                            //check if transaction is from current user UID
                            if(!dataSnapshot.getKey().endsWith(firebaseAuth.getCurrentUser().getUid())) {
                                continue;
                            }
                            transaction = dataSnapshot.getValue(Transaction.class);

                            //if filter date is enable, check transaction if after filter date
                            if(filterDate != null) {
                                try {
                                    if (transaction.type != "Saving Goal" && transaction.type != "Withdrawal - Savings Goal") {
                                        date = simpleDateFormatSavingsGoal.parse(transaction.datetime);
                                    } else {
                                        date = simpleDateFormat.parse(transaction.datetime);
                                    }

                                    if(date.after(filterDate) || simpleDateFormat.format(date).equals(simpleDateFormat.format(filterDate))) {
                                        newTransactions.add(transaction);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                newTransactions.add(transaction);
                            }
                        }

                        if(!newTransactions.isEmpty()) {
                            transactions.clear();
                            transactions.addAll(newTransactions);
                            recylclerViewTransactionsAdapter.notifyDataSetChanged();
                            frameLayout.setVisibility(View.VISIBLE);
                            textViewResults.setVisibility(View.GONE);
                        } else {
                            transactions.clear();
                            recylclerViewTransactionsAdapter.notifyDataSetChanged();
                            frameLayout.setVisibility(View.GONE);
                            textViewResults.setVisibility(View.VISIBLE);
                            textViewResults.setText("No transactions");
                        }
                    }
                }
            }
        });
    }

    private void getTransactionsFilterBy(String transactionType) {
        frameLayout.setVisibility(View.GONE);
        textViewResults.setVisibility(View.VISIBLE);
        textViewResults.setText("Loading transactions...");

        //get all transactions
        database.child("transactions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm");
                SimpleDateFormat simpleDateFormatSavingsGoal = new SimpleDateFormat("MMMM d, yyyy");
                Transaction transaction;
                Date date;
                Calendar calendar;
                List<Transaction> newTransactions = new ArrayList<Transaction>();

                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get transactions", firebaseAuth.getCurrentUser().getUid());
                    database.child("logs").child(database.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getActivity(), "Cannot get your transactions, please try again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), DashboardActivity.class);
                    startActivity(intent);
                } else {
                    if(task.getResult().getChildrenCount() > 0) {
                        for (DataSnapshot dataSnapshot: task.getResult().getChildren()) {
                            //check if transaction is from current user UID
                            if(!dataSnapshot.getKey().endsWith(firebaseAuth.getCurrentUser().getUid())) {
                                continue;
                            }
                            transaction = dataSnapshot.getValue(Transaction.class);

                            if (!transaction.type.equals(transactionType)) {
                                continue;
                            }

                            //if filter date is enable, check transaction if after filter date
                            if(filterDate != null) {
                                try {
                                    if (transaction.type != "Saving Goal" && transaction.type != "Withdrawal - Savings Goal") {
                                        date = simpleDateFormatSavingsGoal.parse(transaction.datetime);
                                    } else {
                                        date = simpleDateFormat.parse(transaction.datetime);
                                    }

                                    if(date.after(filterDate) || simpleDateFormat.format(date).equals(simpleDateFormat.format(filterDate))) {
                                        newTransactions.add(transaction);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                newTransactions.add(transaction);
                            }
                        }

                        if(!newTransactions.isEmpty()) {
                            transactions.clear();
                            transactions.addAll(newTransactions);
                            recylclerViewTransactionsAdapter.notifyDataSetChanged();
                            frameLayout.setVisibility(View.VISIBLE);
                            textViewResults.setVisibility(View.GONE);
                        } else {
                            transactions.clear();
                            recylclerViewTransactionsAdapter.notifyDataSetChanged();
                            frameLayout.setVisibility(View.GONE);
                            textViewResults.setVisibility(View.VISIBLE);
                            textViewResults.setText("No transactions");
                        }
                    }
                }
            }
        });
    }
}