package com.example.exitpro.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.exitpro.Utils.CaptureActUtil;
import com.example.exitpro.Config.Config;
import com.example.exitpro.Utils.FingerprintAuthHelperUtil;
import com.example.exitpro.GlobalVariables;
import com.example.exitpro.R;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    // UI elements
    Button btnOut, btnIn, btnLate, btnLogOut;
    RelativeLayout hHomeLayout;

    // Variables
    int scanNumber = -1;
    String destination = "";
    GlobalVariables globalVariables = new GlobalVariables();
    private ProgressDialog progressDialog;
    FingerprintAuthHelperUtil fingerprintAuthHelperUtil;

    // URLs for API requests
    public static String outURL = Config.BASE_URL + "/student/gate/exit";
    public static String inURL = Config.BASE_URL + "/student/gate/entry/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI elements
        btnOut = findViewById(R.id.btnOut);
        btnIn = findViewById(R.id.btnIn);
        btnLate = findViewById(R.id.btnLate);
        btnLogOut = findViewById(R.id.btnlogOut);
        hHomeLayout = findViewById(R.id.homeLayout);

        // Initialize fingerprint authentication
        fingerprintAuthHelperUtil = new FingerprintAuthHelperUtil(this, hHomeLayout);
        fingerprintAuthHelperUtil.authenticate();

        // Set up button listeners
        setupButtonListeners();

        // Check if the user is logged in
        if (!isLoggedIn()) {
            redirectToLoginActivity();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        fingerprintAuthHelperUtil.authenticate();
    }

    private void setupButtonListeners() {
        // Logout button listener
        btnLogOut.setOnClickListener(view -> logout());

        // Scan out button listener
        btnOut.setOnClickListener(view -> {
            scanNumber = -1;
            destination = "";
            startScan(outScan);
        });

        // Scan in button listener
        btnIn.setOnClickListener(view -> {
            scanNumber = -1;
            startScan(inScan);
        });

        // Latecomers button listener
        btnLate.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, LateComersActivity.class);
            startActivity(intent);
        });
    }

    private void startScan(ActivityResultLauncher<ScanOptions> scanLauncher) {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(false);
        options.setPrompt("Scan a barcode");
        options.setCameraId(0); // Use a specific camera of the device
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        options.setCaptureActivity(CaptureActUtil.class);
        scanLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> outScan = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    scanNumber = Integer.parseInt(result.getContents());
                    showDestinationDialog(String.valueOf(scanNumber));
                }
            });

    private final ActivityResultLauncher<ScanOptions> inScan = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    scanNumber = Integer.parseInt(result.getContents());
                    handleInScan();
                }
            });

    private void handleInScan() {
        showLoadingDialog();
        JSONObject jsonRequest = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);

        // Create and send the JSON request for in scan
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                inURL + scanNumber,
                jsonRequest,
                response -> {
                    dismissLoadingDialog();
                    handleInScanResponse(response);
                },
                error -> {
                    dismissLoadingDialog();
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }

    private void handleInScanResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("isSuccess");
            if (success) {
                showSuccessDialog();
            } else {
                Toast.makeText(getApplicationContext(), "STUDENT IS INSIDE CAMPUS!", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true); // Move the task containing this activity to the back of the activity stack
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.contains("access_token");
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("access_token");
        editor.apply();
        redirectToLoginActivity();
    }

    private void redirectToLoginActivity() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDestinationDialog(final String scannedBarcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Destination");

        final EditText destinationInput = new EditText(this);
        builder.setView(destinationInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            destination = destinationInput.getText().toString();
            if (!destination.isEmpty()) {
                sendOutScanRequest();
            } else {
                Toast.makeText(getApplicationContext(), "DESTINATION IS INVALID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void sendOutScanRequest() {
        showLoadingDialog();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("roll_number", scanNumber);
            jsonObject.put("goingTo", destination);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, outURL, jsonObject,
                response -> {
                    dismissLoadingDialog();
                    handleOutScanResponse(response);
                },
                error -> {
                    dismissLoadingDialog();
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonRequest);
    }

    private void handleOutScanResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("isSuccess");
            if (success) {
                showSuccessDialog();
            } else {
                Toast.makeText(getApplicationContext(), "STUDENT ALREADY OUT!", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.success_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        new Handler().postDelayed(() -> dialog.dismiss(), 2000);
    }
}
