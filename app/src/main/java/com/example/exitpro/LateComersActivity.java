package com.example.exitpro;

//import com.example.exitpro.LateAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.view.LayoutInflater;

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

    public static String lateURL = "https://85ae-169-149-230-206.ngrok-free.app/exitPro/student/late";

    GlobalVariables globalVariables = new GlobalVariables();
    ArrayList<LateStudent> lateList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late_comers);

        showLoadingDialog();
        JSONArray jsonRequest = new JSONArray();
        RequestQueue queue = Volley.newRequestQueue(LateComersActivity.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                lateURL,
                null,
                response -> {
                    dismissLoadingDialog();
                    try {
                        // Iterate through the array to access individual objects
//                        Toast.makeText(getApplicationContext(), "Success - > " + response.toString(), Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            // Access individual fields
                            LateStudent stu = new LateStudent();
                            stu.setPhoneNumber(jsonObject.getString("contact"));
                            stu.setName(jsonObject.getString("name"));
                            stu.setRollNumber(jsonObject.getInt("roll_number"));
                            stu.setDestination(jsonObject.getString("goingTo"));
                            String outTime = jsonObject.getString("outTime");

                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault());
                            Date date = sdf.parse(outTime);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);

                            stu.setYear(calendar.get(Calendar.YEAR));
                            stu.setMonth(calendar.get(Calendar.MONTH) + 1); // Note: Month is zero-based
                            stu.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                            stu.setHour(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
                            stu.setMinute(String.format("%02d", calendar.get(Calendar.MINUTE)));
                            stu.setSecond(String.format("%02d", calendar.get(Calendar.SECOND)));
                            lateList.add(stu);
                        }
                        globalVariables.setlateList(lateList);

                        // Update the UI with the lateList data
                        updateUIWithLateList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    dismissLoadingDialog();
                    Toast.makeText(getApplicationContext(), "ERROR - > " + error.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("sayak", error.toString());
                });

        queue.add(jsonArrayRequest);
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

        // Log the lateList to check if it's correctly populated
//        Log.d("abhay", String.valueOf(lateList));

        LateAdapter adapter = new LateAdapter(LateComersActivity.this, lateList);
        recyclerView.setAdapter(adapter);
    }

    // ... Other code ...
}
