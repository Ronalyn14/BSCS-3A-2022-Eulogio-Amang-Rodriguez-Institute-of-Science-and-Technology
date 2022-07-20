package com.example.banking.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.banking.MainActivity;
import com.example.banking.Transaction;
import com.example.banking.databinding.FragmentTransactionBinding;
import com.example.banking.transactionAdapter;
import com.example.banking.ui.home.HomeViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransactionFragment extends Fragment {

    private FragmentTransactionBinding binding;
    private ImageView profileView;
    private TextView name,id,funds;
    private DocumentReference customerDatabase;
    transactionAdapter adapter;
    SearchView search;
    private RecyclerView recyclerView;
    Query query;
    Query searchQuery;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransactionViewModel homeViewModel =
                new ViewModelProvider(this).get(TransactionViewModel.class);

        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.transactionRecylcler;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        search = binding.searchBar;

        query = FirebaseFirestore.getInstance()
                .collection("transaction")
                .whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5);

        FirestoreRecyclerOptions<Transaction> options = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();

        adapter = new transactionAdapter(options);
        recyclerView.setAdapter(adapter);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadData();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() >= 1){
                    startSearch(newText);
                }else{
                    loadData();
                }
                return true;
            }
        });

        return root;
    }

    public void startSearch(String SearchText){
        searchQuery = FirebaseFirestore.getInstance()
                .collection("transaction")
                .whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy("reference")
                .startAt(SearchText)
                .endAt(SearchText + '~')
                .limit(50);

        FirestoreRecyclerOptions<Transaction> options
                = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(searchQuery, Transaction.class)
                .build();

        adapter = new transactionAdapter(options);
        recyclerView.setAdapter(adapter);
        onStart();
    }

    public void loadData(){
        FirestoreRecyclerOptions<Transaction> options
                = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();

        adapter = new transactionAdapter(options);
        recyclerView.setAdapter(adapter);
        onStart();
    }

    @Override public void onStart()
    {
        super.onStart();
        adapter.startListening();
    }

    @Override public void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}