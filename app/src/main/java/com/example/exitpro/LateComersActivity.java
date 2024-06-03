package com.example.exitpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LateComersActivity extends AppCompatActivity {

    public static String lateURL = "https://exitpro-backend.onrender.com/student/out/late";

    GlobalVariables globalVariables = new GlobalVariables();
    ArrayList<LateStudent> lateList = new ArrayList<>();
    private ProgressDialog progressDialog;
    FingerprintAuthHelper fingerprintAuthHelper;

    LinearLayout lLatelayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late_comers);
        lLatelayout = findViewById(R.id.lateLayout);
        fingerprintAuthHelper = new FingerprintAuthHelper(this, lLatelayout);

        showLoadingDialog();
        RequestQueue queue = Volley.newRequestQueue(LateComersActivity.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                lateURL,
                null,
                response -> {
                    dismissLoadingDialog();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            LateStudent stu = new LateStudent();
                            stu.setPhoneNumber(jsonObject.getString("contact"));
                            stu.setName(jsonObject.getString("name"));
                            stu.setRollNumber(jsonObject.getInt("roll_number"));
                            stu.setDestination(jsonObject.getString("goingTo"));
                            String outTime = jsonObject.getString("outTime");

                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault());
                            try {
                                Date date = sdf.parse(outTime);
                                if (date != null) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);

                                    stu.setYear(calendar.get(Calendar.YEAR));
                                    stu.setMonth(calendar.get(Calendar.MONTH) + 1); // Note: Month is zero-based
                                    stu.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                                    stu.setHour(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
                                    stu.setMinute(String.format("%02d", calendar.get(Calendar.MINUTE)));
                                    stu.setSecond(String.format("%02d", calendar.get(Calendar.SECOND)));
                                    lateList.add(stu);
                                } else {
                                    Log.e("ParseError", "Date parsing returned null for string: " + outTime);
                                }
                            } catch (ParseException e) {
                                Log.e("ParseError", "Failed to parse date: " + outTime, e);
                            }
                        }
                        globalVariables.setlateList(lateList);
                        updateUIWithLateList();
                    } catch (JSONException e) {
                        Log.e("JSONError", "JSON parsing error", e);
                    }
                },
                error -> {
                    dismissLoadingDialog();
                    Toast.makeText(getApplicationContext(), "ERROR - > " + error.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("RequestError", error.toString());
                });

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        fingerprintAuthHelper.authenticate();
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(LateComersActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void updateUIWithLateList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LateAdapter adapter = new LateAdapter(LateComersActivity.this, lateList);
        recyclerView.setAdapter(adapter);
    }

    // ... Other code ...
}
