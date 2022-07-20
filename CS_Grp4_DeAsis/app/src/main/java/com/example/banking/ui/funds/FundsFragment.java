package com.example.banking.ui.funds;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.banking.FcmNotificationsSender;
import com.example.banking.MainActivity;
import com.example.banking.databinding.FragmentFundsBinding;
import com.example.banking.registration;
import com.example.banking.ui.bills.BillsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FundsFragment extends Fragment {

    private FragmentFundsBinding binding;
    private TextView name,id,funds;
    private EditText acctName, acctID, amount, remark;
    private Button transfer;
    private Integer currentFund;
    private String number, refsmsnum, recipientNameSMS,RecepientNum, token;
    private DocumentReference customerDatabase, recepient;
    private String Code;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FundViewModel slideshowViewModel =
                new ViewModelProvider(this).get(FundViewModel.class);

        binding = FragmentFundsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        name = binding.cusName;
        id = binding.cusID;
        funds = binding.cusFunds;
        transfer = binding.confirm;
        acctName = binding.accountName;
        acctID = binding.accountNumber;
        amount = binding.amount;
        remark = binding.remark;

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

                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (acctName.getText().toString().trim().isEmpty()){
                    acctName.setError("Account Name is required!");
                    acctName.requestFocus();
                    return;
                }

                if (acctID.getText().toString().trim().isEmpty()){
                    acctID.setError("Account number is required!");
                    acctID.requestFocus();
                    return;
                }

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


                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("user_data")
                        .whereEqualTo("id", acctID.getText().toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("TAG", document.getId() + " => " + document.getData());
                                        Integer recepientFund = Integer.parseInt(document.getData().get("funds").toString());
                                        Integer amountTrasfer = Integer.parseInt(amount.getText().toString().trim());
                                        Integer recepientNewFund = recepientFund + amountTrasfer;
                                        Integer UserNewFund = currentFund - amountTrasfer;
                                        funds.setText("Current Funds: ₱"+ UserNewFund.toString());
                                        number = document.getData().get("cellphone").toString();
                                        token = document.getData().get("token").toString();

                                        Map newUserFund = new HashMap();
                                        newUserFund.put("funds", UserNewFund.toString());
                                        customerDatabase.update(newUserFund);

                                        recepient = FirebaseFirestore.getInstance().collection("user_data").document(document.getId());
                                        Map newRecepientFund = new HashMap();
                                        newRecepientFund.put("funds", recepientNewFund.toString());
                                        recepient.update(newRecepientFund);

                                        String transactionID = UUID.randomUUID().toString();
                                        transactionID = transactionID.replaceAll("[^\\d.]", "");
                                        transactionID = transactionID.replaceAll("0", "");
                                        transactionID = transactionID.substring(0,8);

                                        refsmsnum = transactionID;
                                        recipientNameSMS = document.getData().get("name").toString();

                                        RecepientNum = document.getData().get("cellphone").toString();
                                        String DateCreated = new java.sql.Date(System.currentTimeMillis()).toString();

                                        Long timestamp = System.currentTimeMillis()/1000;


                                        Map<String, Object> transaction = new HashMap<>();
                                        transaction.put("reference", transactionID);
                                        transaction.put("type", "Fund Transfer");
                                        transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        transaction.put("remark", remark.getText().toString().trim());
                                        transaction.put("amount", amount.getText().toString().trim());
                                        transaction.put("recepientid", document.getId());
                                        transaction.put("recepientname", document.getData().get("name").toString());
                                        transaction.put("date", DateCreated);
                                        transaction.put("timestamp", timestamp);

                                        Map<String, Object> transactionRecepient = new HashMap<>();
                                        transactionRecepient.put("reference", transactionID);
                                        transactionRecepient.put("type", "Fund Transfer");
                                        transactionRecepient.put("user", document.getId());
                                        transactionRecepient.put("remark", remark.getText().toString().trim());
                                        transactionRecepient.put("amount", amount.getText().toString().trim());
                                        transactionRecepient.put("recepientid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        transactionRecepient.put("recepientname", name.getText().toString());
                                        transactionRecepient.put("date", DateCreated);
                                        transactionRecepient.put("timestamp", timestamp);

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();


                                        db.collection("transaction")
                                                .add(transaction)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        db.collection("transaction")
                                                        .add(transactionRecepient)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                new HTTPReqTaskRecepient().execute();
                                                                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token, "Fund Transfer",  "You have received "+amount.getText().toString()+" from "+name.getText().toString(), getContext(),getActivity());
                                                                notificationsSender.SendNotifications();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("test", "Error adding document", e);
                                                            }
                                                        });

                                                        Toast.makeText(getContext(), "Transfer Success", Toast.LENGTH_LONG).show();
                                                        new HTTPReqTask().execute();
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
                                } else {
                                    Log.d("TAG", "Error getting documents: ", task.getException());
                                    Toast.makeText(getContext(), "Transfer Error", Toast.LENGTH_LONG).show();
                                }
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
                URL url = new URL("http://122.54.191.90:8085/goip_send_sms.html?username=root&password=root&port=2&recipients="+number+"&sms="+ URLEncoder.encode("Hello "+name.getText().toString()+" Good Day! You have successfully transferred "+amount.getText().toString()+" to "+recipientNameSMS+" This is your reference number:"+refsmsnum));
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

    public class HTTPReqTaskRecepient extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://122.54.191.90:8085/goip_send_sms.html?username=root&password=root&port=2&recipients="+RecepientNum+"&sms="+ URLEncoder.encode("Hello "+recipientNameSMS+" Good Day! You have successfully received "+amount.getText().toString()+" from "+name.getText().toString()+" This is your reference number:"+refsmsnum));
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