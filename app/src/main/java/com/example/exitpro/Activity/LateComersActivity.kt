package com.example.exitpro.Activity;

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
import com.example.exitpro.Config.Config;
import com.example.exitpro.GlobalVariables;
import com.example.exitpro.Adapter.LateAdapter;
import com.example.exitpro.Model.LateStudent;
import com.example.exitpro.R;
import com.example.exitpro.Utils.FingerprintAuthHelperUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LateComersActivity extends AppCompatActivity {

    // URL for fetching late students
    public static String lateURL = Config.BASE_URL + "/student/out/late";

    // Variables
    GlobalVariables globalVariables = new GlobalVariables();
    ArrayList<LateStudent> lateList = new ArrayList<>();
    private ProgressDialog progressDialog;
    FingerprintAuthHelperUtil fingerprintAuthHelperUtil;

    // UI element
    LinearLayout lLatelayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late_comers);

        // Initialize UI elements
        lLatelayout = findViewById(R.id.lateLayout);

        // Initialize fingerprint authentication
        fingerprintAuthHelperUtil = new FingerprintAuthHelperUtil(this, lLatelayout);

        // Show loading dialog
        showLoadingDialog();

        // Fetch the list of late students
        fetchLateStudents();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        fingerprintAuthHelperUtil.authenticate();
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

    private void fetchLateStudents() {
        RequestQueue queue = Volley.newRequestQueue(LateComersActivity.this);

        // Create and send JSON array request to fetch late students
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                lateURL,
                null,
                response -> {
                    dismissLoadingDialog();
                    handleResponse(response);
                },
                error -> {
                    dismissLoadingDialog();
                    handleError(error);
                });

        queue.add(jsonArrayRequest);
    }

    private void handleResponse(org.json.JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                LateStudent student = parseLateStudent(jsonObject);
                if (student != null) {
                    lateList.add(student);
                }
            }
            globalVariables.setLateList(lateList);
            updateUIWithLateList();
        } catch (JSONException e) {
            Log.e("JSONError", "JSON parsing error", e);
        }
    }

    private LateStudent parseLateStudent(JSONObject jsonObject) {
        try {
            LateStudent student = new LateStudent();
            student.setPhoneNumber(jsonObject.getString("contact"));
            student.setName(jsonObject.getString("name"));
            student.setRollNumber(jsonObject.getInt("roll_number"));
            student.setDestination(jsonObject.getString("goingTo"));

            String outTime = jsonObject.getString("outTime");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(outTime);

            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                student.setYear(calendar.get(Calendar.YEAR));
                student.setMonth(calendar.get(Calendar.MONTH) + 1); // Note: Month is zero-based
                student.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                student.setHour(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
                student.setMinute(String.format("%02d", calendar.get(Calendar.MINUTE)));
                student.setSecond(String.format("%02d", calendar.get(Calendar.SECOND)));
                return student;
            } else {
                Log.e("ParseError", "Date parsing returned null for string: " + outTime);
            }
        } catch (JSONException | ParseException e) {
            Log.e("ParseError", "Failed to parse late student", e);
        }
        return null;
    }

    private void handleError(Throwable error) {
        Toast.makeText(getApplicationContext(), "ERROR - > " + error.toString(), Toast.LENGTH_SHORT).show();
        Log.e("RequestError", error.toString());
    }

    private void updateUIWithLateList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LateAdapter adapter = new LateAdapter(LateComersActivity.this, lateList);
        recyclerView.setAdapter(adapter);
    }
}
