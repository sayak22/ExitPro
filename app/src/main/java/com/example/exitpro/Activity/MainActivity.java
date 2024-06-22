package com.example.exitpro.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.example.exitpro.Utils.FingerprintAuthHelperUtil;
import com.example.exitpro.R;

public class MainActivity extends AppCompatActivity {

    // Declare FingerprintAuthHelperUtil object
    private FingerprintAuthHelperUtil fingerprintAuthHelperUtil;
    private RelativeLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize layout elements
        mMainLayout = findViewById(R.id.mainLayout);

        // If user is not logged in, redirect to login activity
        if (!isLoggedIn()) {
            redirectToLoginActivity();
            finish();
        } else {
            // Otherwise, redirect to home activity
            redirectToHomeActivity();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Authenticate using fingerprint
        fingerprintAuthHelperUtil.authenticate();

        // If user is not logged in, redirect to login activity
        if (!isLoggedIn()) {
            redirectToLoginActivity();
            finish();
        } else {
            // Otherwise, redirect to home activity
            redirectToHomeActivity();
        }
    }

    /**
     * Check if the user is logged in by verifying the presence of an access token.
     *
     * return true if the access token is available, false otherwise.
     */
    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.contains("access_token");
    }

    /**
     * Redirect to the LoginActivity.
     */
    private void redirectToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the MainActivity so the user cannot navigate back to it
    }

    /**
     * Redirect to the HomeActivity.
     */
    private void redirectToHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}
