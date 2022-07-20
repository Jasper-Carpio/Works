package com.example.primobank.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.primobank.R;
import com.example.primobank.activities.BankTransferActivity;
import com.example.primobank.activities.BuyLoadActivity;
import com.example.primobank.activities.CashInActivity;
import com.example.primobank.activities.PayBillsActivity;
import com.example.primobank.activities.QRCodeScannerActivity;
import com.example.primobank.activities.SavingGoalActivity;
import com.example.primobank.activities.SendMoneyActivity;
import com.example.primobank.activities.SplashScreenActivity;
import com.example.primobank.model.Log;
import com.example.primobank.model.Transaction;
import com.example.primobank.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DashboardHomeFragment extends Fragment {

    private ConstraintLayout constraitLayoutSendMoney;
    private ConstraintLayout constraitLayoutCashIn;
    private ConstraintLayout constraitLayoutBuyLoad;
    private ConstraintLayout constraitLayoutBankTransfer;
    private ConstraintLayout constraitLayoutPayBills;
    private ConstraintLayout constraitLayoutSavingGoal;

    private ProgressDialog progressDialog;

    private TextView textViewBalance;
    private ImageView imageViewCashIn;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private double availableAmount;

    public DashboardHomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_home, container, false);

        constraitLayoutSendMoney = (ConstraintLayout) view.findViewById(R.id.constraitLayoutSendMoney);
        constraitLayoutCashIn = (ConstraintLayout) view.findViewById(R.id.constraitLayoutCashIn);
        constraitLayoutBuyLoad = (ConstraintLayout) view.findViewById(R.id.constraitLayoutBuyLoad);
        constraitLayoutBankTransfer = (ConstraintLayout) view.findViewById(R.id.constraitLayoutBankTransfer);
        constraitLayoutPayBills = (ConstraintLayout) view.findViewById(R.id.constraitLayoutPayBills);
        constraitLayoutSavingGoal = (ConstraintLayout) view.findViewById(R.id.constraitLayoutSavingGoal);

        progressDialog = new ProgressDialog(getActivity());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        imageViewCashIn = (ImageView) view.findViewById(R.id.imageViewCashIn);
        textViewBalance = (TextView) view.findViewById(R.id.textViewBalance);

        constraitLayoutSendMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check available amount is greater than 0
                if (availableAmount < 1) {
                    Toast.makeText(getActivity(), "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
                startActivity(intent);
            }
        });

        constraitLayoutCashIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CashInActivity.class);
                startActivity(intent);
            }
        });

        imageViewCashIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CashInActivity.class);
                startActivity(intent);
            }
        });

        constraitLayoutBuyLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check available amount is greater than 0
                if (availableAmount < 1) {
                    Toast.makeText(getActivity(), "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), BuyLoadActivity.class);
                startActivity(intent);
            }
        });

        constraitLayoutBankTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check available amount is greater than 0
                if (availableAmount < 1) {
                    Toast.makeText(getActivity(), "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), BankTransferActivity.class);
                startActivity(intent);
            }
        });

        constraitLayoutPayBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check available amount is greater than 0
                if (availableAmount < 1) {
                    Toast.makeText(getActivity(), "You do not have enough balance, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), PayBillsActivity.class);
                startActivity(intent);
            }
        });

        constraitLayoutSavingGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SavingGoalActivity.class);
                startActivity(intent);
            }
        });

        getAvailableBalance();

        return view;
    }

    private void getAvailableBalance() {
        progressDialog.setMessage("Getting available balance...");
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
                DecimalFormat decimalFormat = new DecimalFormat("0,000.00");
                double amount = 0;

                if (!task.isSuccessful()) {
                    //create system log
                    Log log = new Log("User failed to get available balance", firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("logs").child(databaseReference.child("logs").push().getKey()).setValue(log);
                    Toast.makeText(getActivity(), "Cannot get your available balance, please login again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
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

                        textViewBalance.setText(decimalFormat.format(amount).toString());
                        availableAmount = amount;
                    }
                }
                progressDialog.dismiss();
            }
        });
    }
}