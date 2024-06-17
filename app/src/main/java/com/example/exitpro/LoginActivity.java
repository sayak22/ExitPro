package com.example.exitpro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;
    EditText guardID;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    String loginURL = Config.BASE_URL + "/security/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.login_button);
        guardID =  findViewById(R.id.guard_id_input);
        requestQueue = Volley.newRequestQueue(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get guardId from the EditText
                String guardId = guardID.getText().toString();

                // Validate guardId (add your own validation logic if needed)

                // Send guardId to the backend
                sendGuardID(guardId);
            }
        });
    }

    private void sendGuardID(String guardId) {
        showLoadingDialog();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("guardId", guardId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                loginURL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                                dismissLoadingDialog();
                        try {
                            boolean isSuccess = response.getBoolean("isSuccess");
                            if (isSuccess) {
//                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                                startActivity(intent);
                                    otpVerificationFragment();
//                                finish();
                            } else {
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
                        dismissLoadingDialog();
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(request);
    }

    private void otpVerificationFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("Guard ID", guardID.getText().toString());
        OTPVerification otpVerificationFragment = new OTPVerification();
        otpVerificationFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.otpVerificationFragment, otpVerificationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}