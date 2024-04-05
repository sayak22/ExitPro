package com.example.exitpro;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private FingerprintAuthHelper fingerprintAuthHelper;
    RelativeLayout mMainLayout ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainLayout=findViewById(R.id.mainLayout);
//        fingerprintAuthHelper = new FingerprintAuthHelper(this, mMainLayout);
//        fingerprintAuthHelper.authenticate();


        if (!isLoggedIn()) {
            // If not logged in, redirect to the login activity
            redirectToLoginActivity();
            finish();
        }
        else{
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fingerprintAuthHelper.authenticate();
        if (!isLoggedIn()) {
            // If not logged in, redirect to the login activity
            redirectToLoginActivity();
            finish();
        }
        else{
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }


    private boolean isLoggedIn () {
        // Check if access token is available
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.contains("access_token");
    }

    private void redirectToLoginActivity () {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the MainActivity so the user cannot navigate back to it
    }
}