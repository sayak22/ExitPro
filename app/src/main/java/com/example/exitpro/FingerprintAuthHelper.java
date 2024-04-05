package com.example.exitpro;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class FingerprintAuthHelper {

    private final Context context;
    private final BiometricPrompt biometricPrompt;
    FingerprintAuthHelper fingerprintAuthHelper;


    View layout;

    public FingerprintAuthHelper(Context context, View mMainLayout) {
        this.context = context;
        layout=mMainLayout;
        Executor executor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            executor = context.getMainExecutor();
        }
        assert executor != null;
        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
//                Toast.makeText(context.getApplicationContext(), "Problem with fingerprint hardware!", Toast.LENGTH_SHORT).show();
                endActivity();
//                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode==BiometricPrompt.ERROR_CANCELED) {
//                    // Handle cancelation
//                    ((FragmentActivity) context).finishAffinity();
//
//                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Handle successful authentication
//                mMainLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Handle authentication failure
                ((FragmentActivity) context).finish(); // Close the activity upon failed authentication
            }


        });
    }

    private void endActivity() {
        ((FragmentActivity) context).finishAffinity();
    }

    public void authenticate() {

        BiometricManager biometricManager = BiometricManager.from(context.getApplicationContext());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(context.getApplicationContext(), "Fingerprint Hardware missing!", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(context.getApplicationContext(), "Fingerprint Hardware not working!", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(context.getApplicationContext(), "No Fingerprint assigned!", Toast.LENGTH_SHORT).show();
                break;
            default:
                layout.setVisibility(View.VISIBLE);
//                Toast.makeText(context.getApplicationContext(), "All OK!", Toast.LENGTH_SHORT).show();

        }

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Scan your fingerprint to unlock")
                .setNegativeButtonText("Cancel")
                .build();



        biometricPrompt.authenticate(promptInfo);


//    private final BiometricPrompt.AuthenticationCallback biometricAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
//        @Override
//        public void onAuthenticationError(int errorCode, CharSequence errString) {
//            super.onAuthenticationError(errorCode, errString);
//            Toast.makeText(context.getApplicationContext(), "Problem with fingerprint hardware!", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//            super.onAuthenticationSucceeded(result);
//            // Handle successful authentication
//
//
//        }
//
//        @Override
//        public void onAuthenticationFailed() {
//            super.onAuthenticationFailed();
//            // Handle authentication failure
//            ((FragmentActivity) context).finish(); // Close the activity upon failed authentication
//        }
//    };
    }
};