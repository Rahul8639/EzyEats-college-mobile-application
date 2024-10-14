package com.example.ezyeatsapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView , ForgetPass;
    DatabaseReference userRef;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), DashBoard.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Connect to the XML layout
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword = findViewById(R.id.passwordInput);
        buttonLogin = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.regprogressbar);
        textView = findViewById(R.id.regNow);
        ForgetPass = findViewById(R.id.forgotPassword);
        TextView loginTextView = findViewById(R.id.textViewMain);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Check for internet connection using the utility class
                if (!NetworkUtil.isInternetAvailable(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        TextPaint paint = loginTextView.getPaint();
        float width = paint.measureText(loginTextView.getText().toString());

        Shader textShader = new LinearGradient(
                0, 0, width, loginTextView.getTextSize(),
                new int[]{
                        Color.parseColor("#FEA32F"), // orange
                        Color.parseColor("#FE8630"), // dark
                        Color.parseColor("#F83C38")  // red
                },
                null, Shader.TileMode.CLAMP);

        loginTextView.getPaint().setShader(textShader);

        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        ForgetPass.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        ForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(editTextEmail.getText());

                // Check for internet connection using the utility class
                if (!NetworkUtil.isInternetAvailable(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }
                else {

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(LoginActivity.this, "Enter Email First", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!(email.endsWith("edu.in") || email.endsWith(".vendor@gmail.com"))) {   // Ensure email check
                        // Snackbar for invalid email
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Enter a Valid Email", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(ContextCompat.getColor(LoginActivity.this, R.color.light_red))  // Set red color for error
                                .setTextColor(Color.parseColor("#ffffff"));
                        snackbar.setAction("x", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.setActionTextColor(Color.parseColor("#ffffff"));
                        snackbar.show();
                        return;
                    }
                }

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Reset Email Sent.", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(ContextCompat.getColor(LoginActivity.this, R.color.green))  // Set red color for error
                                    .setTextColor(Color.parseColor("#ffffff"));
                            snackbar.setAction("x", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.setActionTextColor(Color.parseColor("#ffffff"));
                            snackbar.show();
                            return;
                        } else {
                            Toast.makeText(LoginActivity.this, "Reset Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check for internet connection using the utility class
                if (!NetworkUtil.isInternetAvailable(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }

                // Dismiss the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }

                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }

                if (!(email.endsWith("edu.in") || email.endsWith(".vendor@gmail.com"))) {
                    Toast.makeText(LoginActivity.this, "Enter a Valid Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }

                // Show progress bar only when inputs are valid
                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    // Get the logged-in user's UID and email
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String uid = user.getUid();
                                        String userEmail = user.getEmail();

                                        // Check if email ends with ".vendor@gmail.com"
                                        if (userEmail != null && userEmail.endsWith(".vendor@gmail.com")) {
                                            // Redirect to AdminMain.java
                                            Intent intent = new Intent(LoginActivity.this, AdminPannel.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        // Check if email ends with "edu.in"
                                        else if (userEmail != null && userEmail.endsWith("edu.in")) {
                                            // Check the database for user's phone number verification
                                            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    // Check if the phoneNumber field exists
                                                    if (snapshot.exists() && snapshot.hasChild("phoneNumber")) {
                                                        // Phone number is verified, redirect to MainActivity
                                                        Intent intent = new Intent(LoginActivity.this, DashBoard.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Phone number is not verified, redirect to SelectUniversity
                                                        Intent intent = new Intent(LoginActivity.this, SelectUniversity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    // Handle potential errors here
                                                    Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            // Handle other cases if necessary, or proceed with regular login flow
                                            Toast.makeText(LoginActivity.this, "Invalid email domain.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    // Authentication failed
                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                            .setBackgroundTint(ContextCompat.getColor(LoginActivity.this, R.color.light_red))  // Set red color for error
                                            .setTextColor(Color.parseColor("#ffffff"));
                                    snackbar.setAction("x", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.parseColor("#ffffff"));
                                    snackbar.show();
                                }
                            }
                        });
            }
        });



    }
}
