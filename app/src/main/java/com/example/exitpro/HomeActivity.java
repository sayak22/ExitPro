package com.example.exitpro;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    Button btnOut, btnIn, btnLate, btnLogOut;
    int scanNumber = -1;
    String destination = "";
    RelativeLayout hHomeLayout;
    GlobalVariables globalVariables = new GlobalVariables();
    private ProgressDialog progressDialog;
    FingerprintAuthHelper fingerprintAuthHelper;

    public static String outURL = "https://exitpro-backend.onrender.com/student/gate/exit";
    public static String inURL = "https://exitpro-backend.onrender.com/student/gate/entry/";//add roll number at the end of it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnOut = findViewById(R.id.btnOut);
        btnIn = findViewById(R.id.btnIn);
        btnLate = findViewById(R.id.btnLate);
        btnLogOut = findViewById(R.id.btnlogOut);
        hHomeLayout=findViewById(R.id.homeLayout);
        fingerprintAuthHelper = new FingerprintAuthHelper(this, hHomeLayout);
        fingerprintAuthHelper.authenticate();

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        if (!isLoggedIn()) {
            // If not logged in, redirect to the login activity
            redirectToLoginActivity();
        }

        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNumber = -1;
                destination = "";
                ScanOptions options = new ScanOptions();
                options.setOrientationLocked(false);
                options.setPrompt("Scan a barcode");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(false);
                options.setBarcodeImageEnabled(true);
                options.setCaptureActivity(CaptureAct.class);
                outScan.launch(options);
            }
        });

        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNumber = -1;
                ScanOptions options = new ScanOptions();
                options.setOrientationLocked(false);
                options.setPrompt("Scan a barcode");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(false);
                options.setBarcodeImageEnabled(true);
                options.setCaptureActivity(CaptureAct.class);
                inScan.launch(options);
            }
        });

        btnLate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, LateComersActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onRestart() {
        super.onRestart();

        fingerprintAuthHelper.authenticate();
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
                    showLoadingDialog();
//                    Toast.makeText(getApplicationContext(), "Roll Number -> " + scanNumber, Toast.LENGTH_SHORT).show();
                    JSONObject jsonRequest = new JSONObject();
                    RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.PUT,
                            inURL + scanNumber,
                            jsonRequest,
                            response -> {
                                dismissLoadingDialog();
//                              Toast.makeText(getApplicationContext(),"Success - > "+ response.toString(),Toast.LENGTH_SHORT).show();
                                boolean success;
                                try {
                                    success = response.getBoolean("isSuccess");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                if (success)
                                    showSuccessDialog();
                                else
                                    Toast.makeText(getApplicationContext(),"STUDENT IS INSIDE CAMPUS!",Toast.LENGTH_SHORT).show();
                            },
                            error -> {
                                dismissLoadingDialog();
                                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                            });
                    queue.add(jsonObjectRequest);

                }
            });
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
        // Check if access token is available
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.contains("access_token");
    }

    private void logout() {
        // Clear access token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("access_token");
        editor.apply();

        // Redirect to login activity
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

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showLoadingDialog();
                destination = destinationInput.getText().toString();
                if (!destination.isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Roll Number -> " + scanNumber + "Destination -> " + destination, Toast.LENGTH_SHORT).show();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("roll_number", scanNumber);
                        jsonObject.put("goingTo", destination);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.e("JsonObject", String.valueOf(jsonObject));
                    RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
                    // Create a StringRequest with the POST method.
                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, outURL, jsonObject,
                            response -> {
                                dismissLoadingDialog();
//                                Toast.makeText(getApplicationContext(),"Success - > "+ response.toString(),Toast.LENGTH_SHORT).show();
                                boolean success;
                                try {
                                    success = response.getBoolean("isSuccess");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                if (success)
                                showSuccessDialog();
                                else
                                    Toast.makeText(getApplicationContext(),"STUDENT ALREADY OUT!",Toast.LENGTH_SHORT).show();

                            },
                            error -> {
                                dismissLoadingDialog();
                                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                            }
                    );
                    queue.add(jsonRequest);
                } else {
                    Toast.makeText(getApplicationContext(), "DESTINATION IS INVALID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showSuccessDialog() {
        // Create a custom dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.success_dialog);
        dialog.setCancelable(false);

        // Set the dialog's background to be transparent
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Show the dialog
        dialog.show();

        // Dismiss the dialog after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 2000);
    }
}
