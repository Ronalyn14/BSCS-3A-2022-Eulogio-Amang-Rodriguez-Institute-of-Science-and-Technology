package com.example.banking.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.banking.Transaction;
import com.example.banking.databinding.FragmentHomeBinding;
import com.example.banking.transactionAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
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
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileView = binding.profileImage;
        name = binding.cusName;
        id = binding.cusID;
        funds = binding.cusFunds;

        recyclerView = binding.transactionRecylcler;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        search = binding.searchBar;

        query = FirebaseFirestore.getInstance()
                .collection("transaction")
                .whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20);

        FirestoreRecyclerOptions<Transaction> options = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();

        adapter = new transactionAdapter(options);
        recyclerView.setAdapter(adapter);

        customerDatabase = FirebaseFirestore.getInstance().collection("user_data").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user_data").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        name.setText(document.get("name").toString());
                        id.setText("Acct ID: "+document.get("id").toString());
                        funds.setText("Funds: ₱"+document.get("funds").toString());

                        if(!document.get("profile").toString().equals("default")) {
                            Glide.with(getActivity()).load(document.get("profile").toString()).apply(RequestOptions.circleCropTransform()).into(profileView);
                        }
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

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
                .startAt(SearchText)
                .endAt(SearchText)
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