package com.example.exitpro.utils

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

@RequiresApi(Build.VERSION_CODES.P)
class FingerprintAuthHelperUtil(private val context: Context, private val layout: View) {

    // BiometricPrompt object for authentication
    private val biometricPrompt: BiometricPrompt

    // Executor for running authentication callbacks
    private val executor: Executor = context.mainExecutor

    // Authentication callback handling different authentication outcomes
    private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(
                context.applicationContext,
                "Authentication error: $errString",
                Toast.LENGTH_SHORT
            ).show()
            endActivity()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // Handle successful authentication
            layout.visibility = View.VISIBLE
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            // Handle authentication failure
            (context as FragmentActivity).finish() // Close the activity upon failed authentication
        }
    }

    init {
        // Initialize BiometricPrompt
        biometricPrompt = BiometricPrompt(
            (context as FragmentActivity),
            executor,
            authenticationCallback
        )
    }

    /**
     * End the activity and finish all activities in the task.
     */
    private fun endActivity() {
        (context as FragmentActivity).finishAffinity()
    }

    /**
     * Start the biometric authentication process.
     * Uses strong biometric authenticators for enhanced security.
     */
    fun authenticate() {
        val biometricManager = BiometricManager.from(context)
        
        // Use BIOMETRIC_STRONG for Class 3 biometric security
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    context.applicationContext,
                    "No biometric hardware available",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    context.applicationContext,
                    "Biometric hardware is not available right now",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(context.applicationContext, "No biometric enrolled. Please add biometric authentication in device settings.", Toast.LENGTH_LONG).show()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(context.applicationContext, "Security update required for biometric authentication", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(context.applicationContext, "Biometric authentication is not supported on this device", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric authentication can be performed
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Verify your identity to continue")
                    .setDescription("Use your fingerprint or face to unlock")
                    .setAllowedAuthenticators(authenticators)
                    .setNegativeButtonText("Cancel")
                    .setConfirmationRequired(true)
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}
