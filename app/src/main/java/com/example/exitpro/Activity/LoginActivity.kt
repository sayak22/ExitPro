package com.example.exitpro.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.exitpro.Config.Config;
import com.example.exitpro.Fragment.OTPVerification;
import com.example.exitpro.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    // UI elements
    private Button loginBtn;
    private EditText guardID;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    // URL for login request
    private static final String LOGIN_URL = Config.BASE_URL + "/security/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        loginBtn = findViewById(R.id.login_button);
        guardID = findViewById(R.id.guard_id_input);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Set click listener for the login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get guard ID from EditText
                String guardId = guardID.getText().toString();

                // Validate guard ID (if necessary, add your own validation logic)

                // Send guard ID to the backend
                sendGuardID(guardId);
            }
        });
    }

    /**
     * Sends the guard ID to the backend server.
     *
     * @param guardId The guard ID to send.
     */
    private void sendGuardID(String guardId) {
        // Show loading dialog
        showLoadingDialog();

        // Create JSON object with guard ID
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("guardId", guardId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a request to send the guard ID
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                LOGIN_URL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Dismiss loading dialog
                        dismissLoadingDialog();
                        try {
                            boolean isSuccess = response.getBoolean("isSuccess");
                            if (isSuccess) {
                                // If login is successful, start OTP verification fragment
                                otpVerificationFragment();
                            } else {
                                // If login fails, show error
                                dismissLoadingDialog();
                                guardID.setError("Wrong credentials");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Dismiss loading dialog and show error message
                        dismissLoadingDialog();
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the request queue
        requestQueue.add(request);
    }

    /**
     * Starts the OTP verification fragment.
     */
    private void otpVerificationFragment() {
        // Create a bundle to pass guard ID to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("Guard ID", guardID.getText().toString());

        // Create and set up the OTP verification fragment
        OTPVerification otpVerificationFragment = new OTPVerification();
        otpVerificationFragment.setArguments(bundle);

        // Begin a fragment transaction to replace the current fragment with the OTP verification fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.otpVerificationFragment, otpVerificationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Shows the loading dialog.
     */
    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.show();
    }

    /**
     * Dismisses the loading dialog if it is showing.
     */
    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
