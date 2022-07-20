package com.example.banking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class transactionAdapter extends FirestoreRecyclerAdapter<
        Transaction, transactionAdapter.transactionViewholder> {

    public transactionAdapter(@NonNull FirestoreRecyclerOptions<Transaction> options) {
        super(options);
    }

    @Override
    public transactionViewholder onCreateViewHolder(ViewGroup group, int i) {
        // Create a new instance of the ViewHolder, in this case we are using a custom
        // layout called R.layout.message for each item
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.transaction, group, false);
        return new transactionViewholder(view);
    }


    public class transactionViewholder extends RecyclerView.ViewHolder {
        TextView Reference, Amount, Type, Recepient, Date;
        public transactionViewholder(@NonNull View itemView) {
            super(itemView);

            Recepient = itemView.findViewById(R.id.recepient);
            Date = itemView.findViewById(R.id.Date);
            Amount = itemView.findViewById(R.id.amount);
            Reference = itemView.findViewById(R.id.reference);
            Type = itemView.findViewById(R.id.type);
        }

    }

    @Override
    protected void onBindViewHolder(@NonNull transactionViewholder holder, int position, @NonNull Transaction model) {
        holder.Reference.setText(model.getReference());

        holder.Date.setText(model.getDate());
        holder.Recepient.setText(model.getRecepientname());
        holder.Type.setText(model.getType());
        holder.Amount.setText(model.getAmount());
    }
}
