package com.example.exitpro.Utils

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
     */
    fun authenticate() {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    context.applicationContext,
                    "No fingerprint hardware available",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    context.applicationContext,
                    "Fingerprint hardware is not available right now",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    context.applicationContext,
                    "No fingerprint enrolled. Please add at least one fingerprint in device settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric authentication can be performed
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Scan your fingerprint to unlock")
                    .setNegativeButtonText("Cancel")
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}
