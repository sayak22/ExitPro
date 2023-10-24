package com.example.exitpro;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    Button btnOut;
    Button btnIn;
    int scanNumber = -1;
    String destination = "";
    public static String outAPI = "https://0732-220-158-168-162.ngrok-free.app/smartSystem/student/exit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnOut = findViewById(R.id.btnOut);
        btnIn = findViewById(R.id.btnIn);
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
                barcodeLauncher.launch(options);
            }
        });
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
//                    Toast.makeText(HomeActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    scanNumber = Integer.parseInt(result.getContents());
                    showDestinationDialog(String.valueOf(scanNumber));
                }
            });

    // Launch
    public void onButtonClick(View view) {
        barcodeLauncher.launch(new ScanOptions());
    }

    private void showDestinationDialog(final String scannedBarcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Destination");

        final EditText destinationInput = new EditText(this);
        builder.setView(destinationInput);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                destination = destinationInput.getText().toString();
                if (!destination.isEmpty()) {
                    // Proceed with the API call using Retrofit
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
                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, outAPI, jsonObject,
                            response -> {
                                Toast.makeText(getApplicationContext(),"Success - > "+ response.toString(),Toast.LENGTH_SHORT).show();
//                                listener.onSuccess(response.toString());
                            },
                            error -> {
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            }
                    );
                    queue.add(jsonRequest);
                }
                else {
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
}