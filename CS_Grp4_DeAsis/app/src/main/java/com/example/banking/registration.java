package com.example.banking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.OkHttpClient;
import java.util.UUID;

public class registration extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private EditText fullname,email,pass,number,address;
    private Button registerUser, backtoLogin;
    private ProgressBar progressBar;
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAuth = FirebaseAuth.getInstance();

        backtoLogin = (Button) findViewById(R.id.BackToLogin);
        backtoLogin.setOnClickListener(this);

        registerUser = (Button) findViewById(R.id.Register);
        registerUser.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        fullname = (EditText) findViewById(R.id.editTextName);
        email = (EditText) findViewById(R.id.editTextEmail);
        pass = (EditText) findViewById(R.id.editTextPass);
        number = (EditText) findViewById(R.id.editTextNumber);
        address = (EditText) findViewById(R.id.editTextAddress);

        String url;
        OkHttpClient client = new OkHttpClient();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BackToLogin:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.Register:
                registeruserData();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    private void registeruserData() {
        String name = fullname.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passText = pass.getText().toString().trim();
        String numberText = number.getText().toString().trim();
        String addressText = address.getText().toString().trim();
        String DateText = new java.sql.Date(System.currentTimeMillis()).toString();

        if (name.isEmpty()){
            fullname.setError("Full name is required!");
            fullname.requestFocus();
            return;
        }

        if (emailText.isEmpty()){
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if (passText.isEmpty()){
            pass.setError("Password is required!");
            pass.requestFocus();
            return;
        }

        if(passText.length() < 6){
            pass.setError("Password must be 6 character above!");
            pass.requestFocus();
            return;
        }

        if (numberText.isEmpty()){
            number.setError("Cellphone Number is required!");
            number.requestFocus();
            return;
        }

        if (addressText.isEmpty()){
            address.setError("Address is required!");
            address.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            email.setError("Valid Email is required!");
            email.requestFocus();
            return;
        }

        id = UUID.randomUUID().toString();
        id = id.replaceAll("[^\\d.]", "");
        id = id.replaceAll("0", "");
        id = id.substring(0,8);

        mAuth.createUserWithEmailAndPassword(emailText, passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    User user = new User(name,addressText,emailText,passText,numberText,id,DateText, "default", "0");
                    // Write a message to the database
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("user_data").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(registration.this, "You have been registered successfully", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(registration.this, login.class));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(registration.this, "Database Error", Toast.LENGTH_LONG).show();
                                }
                            });
                }else{
                    Toast.makeText(registration.this, "Database Error", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
