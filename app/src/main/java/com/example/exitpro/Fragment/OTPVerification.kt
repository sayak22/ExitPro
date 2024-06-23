package com.example.exitpro.Fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.exitpro.Activity.HomeActivity
import com.example.exitpro.Config.Config
import com.example.exitpro.R
import org.json.JSONException
import org.json.JSONObject

class OTPVerification : Fragment() {
    // UI elements
    private lateinit var otpEditText: EditText
    private lateinit var verifyButton: Button
    private var progressDialog: ProgressDialog? = null
    private val requestQueue by lazy { Volley.newRequestQueue(activity) }
    private var guardId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_o_t_p_verification, container, false)

        // Initialize UI elements
        otpEditText = rootView.findViewById(R.id.editText_otp)
        verifyButton = rootView.findViewById(R.id.button_verify_otp)

        // Retrieve guard ID from arguments
        guardId = arguments?.getString("Guard ID")

        // Set click listener for the verify button using a lambda expression
        verifyButton.setOnClickListener { verifyOTP() }

        return rootView
    }

    /**
     * Verifies the OTP entered by the user.
     */
    private fun verifyOTP() {
        // Show loading dialog
        showLoadingDialog()

        // Get the entered OTP
        val otp = otpEditText.text.toString()

        // Create JSON object with guard ID and OTP
        val jsonBody = JSONObject()
        try {
            jsonBody.put("guardId", guardId)
            jsonBody.put("otp", otp)
            Log.d("OTPVerification", "JSON Body: $jsonBody")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Create a request to verify the OTP
        val request = JsonObjectRequest(
            Request.Method.POST, OTP_URL, jsonBody,
            { response: JSONObject ->
                dismissLoadingDialog()
                try {
                    val isSuccess = response.getBoolean("isSuccess")
                    if (isSuccess) {
                        val guardName = response.getString("guardName")
                        saveAccessToken(otp, guardName)
                        val intent = Intent(activity, HomeActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    } else {
                        showError("Wrong OTP")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    showError("Wrong OTP")
                    Log.e("OTPVerification", "JSON Parsing error", e)
                }
            },
            { error: VolleyError ->
                dismissLoadingDialog()
                Toast.makeText(activity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(request)
    }

    /**
     * Saves the access token securely using SharedPreferences.
     *
     * @param otp The access token to save.
     */
    private fun saveAccessToken(otp: String, guardName: String) {
        val sharedPreferences = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.apply {
            putString("otp", otp)
            putString("guard_name", guardName)
            apply()
        }
    }

    /**
     * Shows the loading dialog.
     */
    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(activity).apply {
            setCancelable(false)
            setMessage("Checking OTP...")
            show()
        }
    }

    /**
     * Dismisses the loading dialog if it is showing.
     */
    private fun dismissLoadingDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }

    /**
     * Shows an error message in the OTP edit text and as a toast.
     *
     * @param message The error message to show.
     */
    private fun showError(message: String) {
        otpEditText.error = message
        Toast.makeText(requireActivity().applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val OTP_URL = Config.BASE_URL + "/security/otpMatch"
    }
}
