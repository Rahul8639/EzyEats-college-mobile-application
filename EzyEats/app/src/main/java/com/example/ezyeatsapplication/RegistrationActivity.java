package com.example.ezyeatsapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextName;
    Button buttonReg, buttonOtp;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    ProgressBar progressBar;
    TextView textView, shoplist;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), DashBoard.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);  // Connect this to activity_register.xml

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");

        editTextEmail = findViewById(R.id.emailInput);
        editTextPassword = findViewById(R.id.passwordInput);
        editTextName = findViewById(R.id.nameInput);
        buttonReg = findViewById(R.id.registerButton);
        buttonOtp = findViewById(R.id.verifyOtpButton);
        progressBar = findViewById(R.id.regprogressbar);
        textView = findViewById(R.id.loginBack);
        shoplist = findViewById(R.id.shopList);
        TextView loginTextView = findViewById(R.id.textViewMain);

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

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        shoplist.setPaintFlags(shoplist.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        shoplist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegistrationActivity.this, "Redirecting...", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tally.so/r/mVYOXJ"));
                        startActivity(browserIntent);
                    }
                }, 2000); // 2000 milliseconds delay (2 seconds)
            }
        });

        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check for internet connection using the utility class
                if (!NetworkUtil.isInternetAvailable(RegistrationActivity.this)) {
                    Toast.makeText(RegistrationActivity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }

                String email, password, name;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                name = String.valueOf(editTextName.getText());

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(RegistrationActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegistrationActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.endsWith("edu.in")) {   // Check for edu.in domain
                    Toast.makeText(RegistrationActivity.this, "Enter a Valid Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegistrationActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$"; // Modify length if needed
                if (!password.matches(passwordPattern)) {
                    Toast.makeText(RegistrationActivity.this, "Enter a Valid Password", Toast.LENGTH_LONG).show();
                    return; // Return if the password doesn't meet the requirements
                }

                progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Get the current user
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        // Get the unique user ID (UID) from Firebase Auth
                                        String userId = user.getUid();

                                        // Create a HashMap to store both name and email under the user's UID
                                        HashMap<String, String> userData = new HashMap<>();
                                        userData.put("username", name);  // "username" will store the name
                                        userData.put("email", email);    // "email" will store the email

                                        // Save the data in the Realtime Database under UID
                                        usersRef.child(userId).setValue(userData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Account Created Successfully", Snackbar.LENGTH_LONG)
                                                                    .setBackgroundTint(ContextCompat.getColor(RegistrationActivity.this, R.color.green))
                                                                    .setTextColor(Color.parseColor("#ffffff"));
                                                            snackbar.show();

                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(getApplicationContext(), SelectUniversity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }, 2000);
                                                        } else {
                                                            Toast.makeText(RegistrationActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                            .setBackgroundTint(ContextCompat.getColor(RegistrationActivity.this, R.color.light_red))  // Set red color for error
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
                        });
            }
        });
    }
}

