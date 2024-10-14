package com.example.ezyeatsapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textView = findViewById(R.id.displayText);
        String text = "EASY ● FAST ● FREE";
        SpannableString spannableString = new SpannableString(text);

        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#FD8431")),
                text.indexOf("●"),
                text.indexOf("●") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Second dot (blue color)
        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#F61D3C")),
                text.lastIndexOf("●"),
                text.lastIndexOf("●") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Set the SpannableString to the TextView
        textView.setText(spannableString);

        // Delay the splash screen for 3 seconds, then navigate to MainActivity
        new Handler().postDelayed(() -> {
            // Start the main activity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            // Finish SplashActivity so it's not shown when user presses back
            finish();
        }, 3000);
    }
}

