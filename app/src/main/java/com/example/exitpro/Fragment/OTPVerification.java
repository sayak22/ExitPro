package com.example.exitpro.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.exitpro.Activity.HomeActivity;
import com.example.exitpro.Config.Config;
import com.example.exitpro.R;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPVerification extends Fragment {

    // UI elements
    private EditText otpEditText;
    private Button verifyButton;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    // Guard ID and OTP verification URL
    private String guardId;
    private static final String OTP_URL = Config.BASE_URL + "/security/otpMatch";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_o_t_p_verification, container, false);

        // Initialize UI elements
        otpEditText = rootView.findViewById(R.id.editText_otp);
        verifyButton = rootView.findViewById(R.id.button_verify_otp);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(getActivity());

        // Retrieve guard ID from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            guardId = bundle.getString("Guard ID");
        }

        // Set click listener for the verify button using a lambda expression
        verifyButton.setOnClickListener(v -> verifyOTP());

        return rootView;
    }

    /**
     * Verifies the OTP entered by the user.
     */
    private void verifyOTP() {
        // Show loading dialog
        showLoadingDialog();

        // Get the entered OTP
        String otp = otpEditText.getText().toString();

        // Create JSON object with guard ID and OTP
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("guardId", guardId);
            jsonBody.put("otp", otp);
            Log.d("OTPVerification", "JSON Body: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a request to verify the OTP
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                OTP_URL,
                jsonBody,
                response -> {
                    // Dismiss loading dialog
                    dismissLoadingDialog();
                    try {
                        boolean isSuccess = response.getBoolean("isSuccess");
                        if (isSuccess) {
                            // Save access token and navigate to HomeActivity
                            String guardName = response.getString("guardName");
                            saveAccessToken(otp, guardName);
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            // If OTP is wrong, show error
                            otpEditText.setError("Wrong OTP");
                            Toast.makeText(getActivity().getApplicationContext(), "Wrong OTP!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        otpEditText.setError("Wrong OTP");
                        Toast.makeText(getActivity().getApplicationContext(), "Wrong OTP!", Toast.LENGTH_SHORT).show();
                        Log.e("OTPVerification", "JSON Parsing error", e);
                    }
                },
                error -> {
                    // Dismiss loading dialog and show error message
                    dismissLoadingDialog();
                    Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Add the request to the request queue
        requestQueue.add(request);
    }

    /**
     * Saves the access token securely using SharedPreferences.
     *
     * @param otp The access token to save.
     */
    private void saveAccessToken(String otp, String guardName) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("otp", otp);
        editor.putString("guard_name", guardName);
        editor.apply();
    }

    /**
     * Shows the loading dialog.
     */
    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Checking OTP...");
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
