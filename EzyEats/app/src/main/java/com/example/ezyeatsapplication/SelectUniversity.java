package com.example.ezyeatsapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class SelectUniversity extends AppCompatActivity {

    private Spinner universitySpinner;
    private EditText editTextPhoneNumber, editTextOTP;
    private Button btnSendOTP, btnVerifyOTP, goBackButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_university);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        universitySpinner = findViewById(R.id.university_spinner);
        editTextPhoneNumber = findViewById(R.id.editText);
        editTextOTP = findViewById(R.id.editTextOTP);
        btnSendOTP = findViewById(R.id.sendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        goBackButton = findViewById(R.id.goBack);
        progressBar = findViewById(R.id.regprogressbar);

        // University options
        String[] universities = {"Ajeenkya D.Y Patil University", "Savitribai Phule Pune University", "Symbiosis International University", "MIT World Peace University (MIT-WPU)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, universities);
        universitySpinner.setAdapter(adapter);

        btnSendOTP.setOnClickListener(v -> {

            // Check for internet connection using the utility class
            if (!NetworkUtil.isInternetAvailable(SelectUniversity.this)) {
                Toast.makeText(SelectUniversity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                return;  // Stop further execution
            }

            // Dismiss the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }

            String phoneNumber = editTextPhoneNumber.getText().toString().trim();

            // Automatically add "+91" if the user hasn't included it
            if (!phoneNumber.startsWith("+91")) {
                phoneNumber = "+91" + phoneNumber;
            }

            if (phoneNumber.length() != 13) {  // +91 followed by 10 digits
                Toast.makeText(SelectUniversity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(SelectUniversity.this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            // Auto verification
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SelectUniversity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            verificationId = s;
                            progressBar.setVisibility(View.GONE);
                            editTextOTP.setVisibility(View.VISIBLE);
                            btnVerifyOTP.setVisibility(View.VISIBLE);
                        }
                    }).build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });


        btnVerifyOTP.setOnClickListener(v -> {

            // Check for internet connection using the utility class
            if (!NetworkUtil.isInternetAvailable(SelectUniversity.this)) {
                Toast.makeText(SelectUniversity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                return;  // Stop further execution
            }

            // Dismiss the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }

            String otp = editTextOTP.getText().toString().trim();
            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(SelectUniversity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

            // Get the currently signed-in user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // Link the phone number to the existing user
                currentUser.linkWithCredential(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully linked phone number with email-authenticated account
                        String uid = currentUser.getUid();

                        // Get university and phone number
                        String university = universitySpinner.getSelectedItem().toString();
                        String phoneNumber = editTextPhoneNumber.getText().toString();

                        // Reference to the existing user's node using the same UID from registration
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                        // Now update the university and phone number for the same user
                        userRef.child("phoneNumber").setValue(phoneNumber);
                        userRef.child("university").setValue(university);

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SelectUniversity.this, "Phone Number Verified", Toast.LENGTH_SHORT).show();

                        // Redirect to the main activity
                        Intent intent = new Intent(SelectUniversity.this, DashBoard.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error linking phone number
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SelectUniversity.this, "Failed to link phone number", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SelectUniversity.this, "User not found!", Toast.LENGTH_SHORT).show();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for internet connection using the utility class
                if (!NetworkUtil.isInternetAvailable(SelectUniversity.this)) {
                    Toast.makeText(SelectUniversity.this, "No Internet!", Toast.LENGTH_SHORT).show();
                    return;  // Stop further execution
                }

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
