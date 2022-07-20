package com.example.banking;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

public class login extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private Button register,Login, buttonBiometricsLogin;
    private EditText email, pass;
    private ProgressBar progressBar;
    private static String validateChecker = "test";
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        executor = ContextCompat.getMainExecutor(this);
        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.editTextUsername);
        pass = (EditText) findViewById(R.id.editTextPassword);
        register = findViewById(R.id.Register);
        register.setOnClickListener(this);
        Login = findViewById(R.id.Login);
        Login.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBarLogin);
        buttonBiometricsLogin = findViewById(R.id.Fingerprint);
        buttonBiometricsLogin.setOnClickListener(this);

        biometricPrompt = new BiometricPrompt(login.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Login success!", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(login.this, MainActivity.class));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Use fingerprint to login.")
                .setNegativeButtonText("Use password")
                .build();


        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            buttonBiometricsLogin.setVisibility(View.VISIBLE);
            biometricPrompt.authenticate(promptInfo);
        }else{
            buttonBiometricsLogin.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Register:
                startActivity(new Intent(this, registration.class));
                break;
            case R.id.Login:
                validateUser();
                break;
            case R.id.Fingerprint:
                biometricPrompt.authenticate(promptInfo);
                break;
        }
    }

    private void validateUser() {
        String Email = email.getText().toString().trim();
        String Password = pass.getText().toString().trim();

        if (Email.isEmpty()){
            email.setError("Email is required!");
            email.requestFocus();
            Toast.makeText(login.this, "Email Is Required", Toast.LENGTH_LONG).show();
            return;
        }

        if (Password.isEmpty()){
            pass.setError("Password is required!");
            pass.requestFocus();
            Toast.makeText(login.this, "Password is required", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String uid = "";
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null) {
                        uid = user.getUid();
                    }
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("user_data").document(uid)
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(login.this, "Login Success", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(login.this, MainActivity.class));
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Log.d(TAG, "Cached get failed: ", task.getException());
                                    }
                                }
                            });
                }else{
                    Toast.makeText(login.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

}
