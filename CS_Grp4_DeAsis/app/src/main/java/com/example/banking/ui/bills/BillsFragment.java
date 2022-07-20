package com.example.banking.ui.bills;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.banking.MainActivity;
import com.example.banking.billerspin;
import com.example.banking.databinding.FragmentBillsBinding;
import com.example.banking.ui.funds.FundsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BillsFragment extends Fragment {

    private FragmentBillsBinding binding;
    private Button pay;
    private EditText amount, remark;
    private TextView name,id,funds;
    private Spinner spinner;
    private Integer currentFund;
    private String number, refsmsnum, recipientNameSMS;
    private DocumentReference customerDatabase, recepient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BillsViewModel slideshowViewModel =
                new ViewModelProvider(this).get(BillsViewModel.class);

        binding = FragmentBillsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pay = binding.confirm;
        amount = binding.amount;
        remark = binding.remark;
        name = binding.cusName;
        id = binding.cusID;
        funds = binding.cusFunds;

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
                        funds.setText("Current Funds: ₱"+document.get("funds").toString());
                        currentFund = Integer.parseInt(document.get("funds").toString());
                        number = document.get("cellphone").toString();
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });


        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference subjectsRef = rootRef.collection("biller");
        spinner = binding.billerName;
        List<billerspin> biller = new ArrayList<>();
        subjectsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        billerspin userList = new billerspin();
                        userList.setName(document.getString("name"));
                        userList.setBillerId(document.getId());
                        biller.add(userList);
                    }

                    ArrayAdapter<billerspin> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, biller);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }
            }
        });


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (amount.getText().toString().trim().isEmpty()){
                    amount.setError("Amount is required!");
                    amount.requestFocus();
                    return;
                }

                if (remark.getText().toString().trim().isEmpty()){
                    remark.setError("Remark is required!");
                    remark.requestFocus();
                    return;
                }



                billerspin selectedUser =(billerspin) spinner.getSelectedItem();
                String billerName = selectedUser.getName();
                String billerID = selectedUser.getBillerId();

                Integer amountTrasfer = Integer.parseInt(amount.getText().toString().trim());
                Integer UserNewFund = currentFund - amountTrasfer;

                if (UserNewFund < 0){
                    amount.setError("Insufficient is amount!");
                    amount.requestFocus();
                    return;
                }

                funds.setText("Current Funds: ₱"+ UserNewFund);

                Map newUserFund = new HashMap();
                newUserFund.put("funds", UserNewFund.toString());
                customerDatabase.update(newUserFund);

                String transactionID = UUID.randomUUID().toString();
                transactionID = transactionID.replaceAll("[^\\d.]", "");
                transactionID = transactionID.replaceAll("0", "");
                transactionID = transactionID.substring(0,8);

                refsmsnum = transactionID;

                recipientNameSMS = billerName.trim();

                Long timestamp = System.currentTimeMillis()/1000;

                String DateCreated = new java.sql.Date(System.currentTimeMillis()).toString();

                Map<String, Object> transaction = new HashMap<>();
                transaction.put("reference", transactionID);
                transaction.put("type", "Bills Payment");
                transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                transaction.put("remark", remark.getText().toString().trim());
                transaction.put("amount", amount.getText().toString().trim());
                transaction.put("recepientid", billerID.trim());
                transaction.put("recepientname", billerName.trim());
                transaction.put("date", DateCreated);
                transaction.put("timestamp", timestamp);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("transaction")
                        .add(transaction)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                new HTTPReqTask().execute();
                                Toast.makeText(getContext(), "Bills Payment Success", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getActivity(), MainActivity.class));
                                Log.d("test", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("test", "Error adding document", e);
                            }
                        });
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class HTTPReqTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://122.54.191.90:8085/goip_send_sms.html?username=root&password=root&port=2&recipients="+number+"&sms="+ URLEncoder.encode("Hello "+name.getText().toString()+" Good Day! You have successfully payed "+amount.getText().toString()+" to "+recipientNameSMS+" This is your reference number:"+refsmsnum));
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();
                if (code !=  200) {
                    throw new IOException("Invalid response from server: " + code);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i("data", line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }
    }
}