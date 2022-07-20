package com.example.primobank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.primobank.model.Transaction;

import java.util.List;

public class RecylclerViewTransactionsAdapter  extends RecyclerView.Adapter<RecylclerViewTransactionsAdapter.ViewHolder>{

    private List<Transaction> transactions;
    private LayoutInflater mInflater;

    public RecylclerViewTransactionsAdapter(Context context, List<Transaction> data) {
        this.mInflater = LayoutInflater.from(context);
        this.transactions = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view_transactions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.textViewType.setText(transaction.type);
        holder.textViewAmount.setText(transaction.amount);
        holder.textViewDetails.setText(transaction.datetime);

        if (transaction.type.equals("Bank Transfer") || transaction.type.equals("Cash In")) {
            holder.textViewDetails.append(" | " + transaction.bankName);
        }

        if (transaction.type.equals("Pay Bills")) {
            holder.textViewDetails.append(" | " + transaction.billerName);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewType;
        TextView textViewAmount;
        TextView textViewDetails;

        ViewHolder(View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
        }
    }
}
