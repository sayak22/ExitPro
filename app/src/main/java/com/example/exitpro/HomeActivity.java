package com.example.exitpro;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class HomeActivity extends AppCompatActivity {

    Button btnOut;
    Button btnIn;
    int scanNumber=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnOut= findViewById(R.id.btnOut);
        btnIn= findViewById(R.id.btnIn);
        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNumber=-1;
                scanCode();
            }
        });
    }


    void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press volume up to turn on flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            scanNumber = Integer.parseInt(result.getContents());
            Toast.makeText(getApplicationContext(), "Roll Number -> " + scanNumber, Toast.LENGTH_SHORT).show();
//            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
//            builder.setTitle("Result");
//            builder.setMessage("scanned Number " + scanNumber);
//            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
//            {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i)
//                {
//                    dialogInterface.dismiss();
//                }
//            }).show();
        }
    });

}