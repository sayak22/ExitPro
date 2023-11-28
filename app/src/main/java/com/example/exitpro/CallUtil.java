package com.example.exitpro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.app.Activity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// CallUtil.java
public class CallUtil {

    public static void makeCall(Context context, String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            // Permission already granted, make the call
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        }
    }
}
