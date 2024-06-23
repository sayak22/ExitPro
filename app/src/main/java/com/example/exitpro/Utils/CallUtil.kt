package com.example.exitpro.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CallUtil {

    private static final int REQUEST_CALL_PERMISSION = 1;

    /**
     * Initiates a phone call to the specified phone number.
     *
     * @param context     The context from which the call is initiated.
     * @param phoneNumber The phone number to call.
     */
    public static void makeCall(Context context, String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        } else {
            // Permission already granted, proceed with the call
            initiateCall(context, phoneNumber);
        }
    }

    /**
     * Initiates a phone call using the provided phone number.
     *
     * @param context     The context from which the call is initiated.
     * @param phoneNumber The phone number to call.
     */
    private static void initiateCall(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }
}
