package com.example.exitpro.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

/**
 * Utility for making phone calls with permission handling.
 */
object CallUtil {

    /**
     * Initiates a phone call to the specified phone number.
     * Checks for CALL_PHONE permission before proceeding.
     *
     * @param context     The context from which the call is initiated.
     * @param phoneNumber The phone number to call.
     */
    fun makeCall(context: Context, phoneNumber: String) {
        if (PermissionUtil.checkCallPhonePermission(context)) {
            initiateCall(context, phoneNumber)
        } else {
            Toast.makeText(context, "Phone call permission is required. Please grant it in settings.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Initiates the phone call with proper error handling.
     *
     * @param context     The context from which the call is initiated.
     * @param phoneNumber The phone number to call.
     */
    private fun initiateCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = "tel:${phoneNumber.trim()}".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No phone app available to make calls", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to initiate call: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
