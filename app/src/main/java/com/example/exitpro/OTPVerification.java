package com.example.exitpro;

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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import android.app.ProgressDialog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPVerification extends Fragment {

    private EditText otpEditText;
    private Button verifyButton;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    String guardId;
    String otpURL = "https://exitpro-backend.onrender.com/security/otpMatch";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_o_t_p_verification, container, false);

        otpEditText = rootView.findViewById(R.id.editText_otp);
        verifyButton = rootView.findViewById(R.id.button_verify_otp);
        requestQueue = Volley.newRequestQueue(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            guardId = bundle.getString("Guard ID");
        }
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to verify OTP
                verifyOTP();
            }
        });

        return rootView;
    }

    private void verifyOTP() {
        // Implement OTP verification logic here
        // For example, you can compare the entered OTP with the OTP received from the backend
        // If the OTP matches, you can proceed with the login process
        // Otherwise, display an error message to the user
        showLoadingDialog();
        String otp = otpEditText.getText().toString();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("guardId", guardId);
            jsonBody.put("otp", otp);
            Log.e("SAYAK",jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                otpURL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dismissLoadingDialog();
                        try {
                            boolean isSuccess = response.getBoolean("isSuccess");
                            if (isSuccess) {
                                saveAccessToken(otp);
                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                dismissLoadingDialog();
                                otpEditText.setError("Wrong OTP");
                                Toast.makeText(getActivity().getApplicationContext(), "Wrong OTP!",Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            otpEditText.setError("Wrong OTP");
                            Toast.makeText(getActivity().getApplicationContext(), "Wrong OTP!",Toast.LENGTH_SHORT);
                            Log.e("SAYAK","catch block otpmatch");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dismissLoadingDialog();
                        Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(request);

    }
    private void saveAccessToken(String accessToken) {
        // Save access token securely (e.g., using SharedPreferences)
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
    }
    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Checking OTP...");
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}