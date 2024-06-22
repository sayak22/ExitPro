package com.example.exitpro.Utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class FingerprintAuthHelperUtil {

    private final Context context;
    private final BiometricPrompt biometricPrompt;
    private final View layout;

    public FingerprintAuthHelperUtil(Context context, View mainLayout) {
        this.context = context;
        this.layout = mainLayout;

        Executor executor = context.getMainExecutor();
        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, authenticationCallback);
    }

    private final BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {

        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(context.getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            endActivity();
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            // Handle successful authentication
            layout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            // Handle authentication failure
            ((FragmentActivity) context).finish(); // Close the activity upon failed authentication
        }
    };

    private void endActivity() {
        ((FragmentActivity) context).finishAffinity();
    }

    public void authenticate() {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(context.getApplicationContext(), "No fingerprint hardware available", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(context.getApplicationContext(), "Fingerprint hardware is not available right now", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(context.getApplicationContext(), "No fingerprint enrolled. Please add at least one fingerprint in device settings.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Biometric authentication can be performed
                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Fingerprint Authentication")
                        .setSubtitle("Scan your fingerprint to unlock")
                        .setNegativeButtonText("Cancel")
                        .build();
                biometricPrompt.authenticate(promptInfo);
                break;
        }
    }
}
