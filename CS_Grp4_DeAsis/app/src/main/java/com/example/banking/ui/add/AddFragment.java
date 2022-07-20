package com.example.banking.ui.add;

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

import com.example.banking.FcmNotificationsSender;
import com.example.banking.MainActivity;
import com.example.banking.billerspin;
import com.example.banking.databinding.FragmentAddBinding;
import com.example.banking.ui.bills.BillsFragment;
import com.example.banking.ui.add.AddViewModel;
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

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private Button pay;
    private EditText amount;
    private TextView name,id,funds;
    private Integer currentFund;
    private String number, refsmsnum, recipientNameSMS;
    private DocumentReference customerDatabase, recepient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AddViewModel slideshowViewModel =
                new ViewModelProvider(this).get(AddViewModel.class);

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pay = binding.confirm;
        amount = binding.amount;
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

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (amount.getText().toString().trim().isEmpty()){
                    amount.setError("Amount is required!");
                    amount.requestFocus();
                    return;
                }

                Integer amountTrasfer = Integer.parseInt(amount.getText().toString().trim());
                Integer UserNewFund = currentFund + amountTrasfer;

                funds.setText("Current Funds: ₱"+ UserNewFund);

                Map newUserFund = new HashMap();
                newUserFund.put("funds", UserNewFund.toString());
                customerDatabase.update(newUserFund);

                String transactionID = UUID.randomUUID().toString();
                transactionID = transactionID.replaceAll("[^\\d.]", "");
                transactionID = transactionID.replaceAll("0", "");
                transactionID = transactionID.substring(0,8);

                refsmsnum = transactionID;

                Long timestamp = System.currentTimeMillis()/1000;

                String DateCreated = new java.sql.Date(System.currentTimeMillis()).toString();

                Map<String, Object> transaction = new HashMap<>();
                transaction.put("reference", transactionID);
                transaction.put("type", "Add Fund");
                transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                transaction.put("remark", "Additiong of funds");
                transaction.put("amount", amount.getText().toString().trim());
                transaction.put("recepientid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                transaction.put("recepientname", name.getText().toString().trim());
                transaction.put("date", DateCreated);
                transaction.put("timestamp", timestamp);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("transaction")
                        .add(transaction)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                new HTTPReqTask().execute();
                                Toast.makeText(getContext(), "Add Fund Success", Toast.LENGTH_LONG).show();
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
                URL url = new URL("http://122.54.191.90:8085/goip_send_sms.html?username=root&password=root&port=2&recipients="+number+"&sms="+ URLEncoder.encode("Hello "+name.getText().toString()+" Good Day! You have successfully added "+amount.getText().toString()+" to your account. This is your reference number:"+refsmsnum));
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