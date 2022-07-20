package com.example.banking.ui.appointment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.banking.ImageLoadTask;
import com.example.banking.MainActivity;
import com.example.banking.billerspin;
import com.example.banking.databinding.FragmentAppointBinding;
import com.example.banking.databinding.FragmentFundsBinding;
import com.example.banking.purposespin;
import com.example.banking.timeframespin;
import com.example.banking.ui.bills.BillsFragment;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AppointFragment extends Fragment {

    private FragmentAppointBinding binding;
    private TextView name,id,funds, tranID;
    private EditText dateTxt;
    private Button appoint;
    private Spinner purpose, timeslot;
    private DocumentReference customerDatabase, recepient;
    final Calendar myCalendar= Calendar.getInstance();
    private ImageView imageView;
    private LinearLayout inputView,forView;
    private String number, timeSelected, purposeSelected, transactionID;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        AppointViewModel slideshowViewModel =
                new ViewModelProvider(this).get(AppointViewModel.class);

        binding = FragmentAppointBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        name = binding.cusName;
        id = binding.cusID;
        funds = binding.cusFunds;
        appoint = binding.confirm;
        purpose = binding.purpose;
        dateTxt = binding.date;
        timeslot = binding.timeframe;
        imageView = binding.imageView;
        inputView = binding.inputView;
        forView = binding.forView;
        tranID = binding.tranID;

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
        CollectionReference subjectsRef = rootRef.collection("purpose");
        List<purposespin> purposeArr = new ArrayList<>();
        subjectsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        purposespin purposeList = new purposespin();
                        purposeList.setName(document.getString("name"));
                        purposeList.setPurposeId(document.getId());
                        purposeArr.add(purposeList);
                    }

                    ArrayAdapter<purposespin> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, purposeArr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    purpose.setAdapter(adapter);
                }
            }
        });

        FirebaseFirestore timeframeRef = FirebaseFirestore.getInstance();
        CollectionReference timeRef = timeframeRef.collection("timeframe");
        List<timeframespin> timeframeArr = new ArrayList<>();
        timeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        timeframespin timeList = new timeframespin();
                        timeList.setName(document.getString("name"));
                        timeList.setTimeframeId(document.getId());
                        timeframeArr.add(timeList);
                    }

                    ArrayAdapter<timeframespin> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, timeframeArr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    timeslot.setAdapter(adapter);
                }
            }
        });

        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(),date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        appoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateTxt.getText().toString().trim().isEmpty()){
                    dateTxt.setError("Date is required!");
                    dateTxt.requestFocus();
                    return;
                }

                purposespin selectedUser =(purposespin) purpose.getSelectedItem();
                String purposeName = selectedUser.getName();
                String purposeID = selectedUser.getPurposeId();

                purposeSelected = purposeName;

                timeframespin selectedTime =(timeframespin) timeslot.getSelectedItem();
                String timeName = selectedTime.getName();
                String timeID = selectedTime.getTimeframeId();

                timeSelected = timeName;

                transactionID = UUID.randomUUID().toString();
                transactionID = transactionID.replaceAll("[^\\d.]", "");
                transactionID = transactionID.replaceAll("0", "");
                transactionID = transactionID.substring(0,8);

                inputView.setVisibility(view.GONE);
                tranID.setText("Ticket#:"+transactionID);
                new ImageLoadTask("https://chart.googleapis.com/chart?chs=100x100&cht=qr&chl="+transactionID, imageView).execute();
                forView.setVisibility(view.VISIBLE);

                Long timestamp = System.currentTimeMillis()/1000;

                String DateCreated = new java.sql.Date(System.currentTimeMillis()).toString();

                Map<String, Object> transaction = new HashMap<>();
                transaction.put("reference", transactionID);
                transaction.put("type", "Appointment");
                transaction.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                transaction.put("time", timeName.trim());
                transaction.put("date", dateTxt.getText().toString().trim());
                transaction.put("purpose", purposeName.trim());
                transaction.put("dateCreated", DateCreated);
                transaction.put("timestamp", timestamp);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("appointment")
                        .add(transaction)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                new HTTPReqTask().execute();
                                Toast.makeText(getContext(), "Appointment Confirm", Toast.LENGTH_LONG).show();
//                                startActivity(new Intent(getActivity(), MainActivity.class));
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

    public class HTTPReqTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://122.54.191.90:8085/goip_send_sms.html?username=root&password=root&port=2&recipients="+number+"&sms="+ URLEncoder.encode("Hello "+name.getText().toString()+" Good Day! You have successfully scheduled on "+dateTxt.getText().toString()+" "+ timeSelected +". With a purpose of "+purposeSelected+". This is your appointment number:"+transactionID));
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

    private void updateLabel(){
        String myFormat="MM-dd-yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        dateTxt.setText(dateFormat.format(myCalendar.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}