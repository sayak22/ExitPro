package com.example.exitpro.Activity

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.exitpro.Config.Config
import com.example.exitpro.Fragment.OTPVerification
import com.example.exitpro.R
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var guardID: EditText
    private lateinit var progressDialog: ProgressDialog
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        loginBtn = findViewById(R.id.login_button)
        guardID = findViewById(R.id.guard_id_input)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Set click listener for the login button
        loginBtn.setOnClickListener {
            val guardId = guardID.text.toString().trim()

            if (guardId.isNotEmpty()) {
                sendGuardID(guardId)
            } else {
                guardID.error = "Guard ID cannot be empty"
            }
        }
    }

    private fun sendGuardID(guardId: String) {
        showLoadingDialog()

        val jsonBody = JSONObject()
        try {
            jsonBody.put("guardId", guardId)
        } catch (e: JSONException) {
            e.printStackTrace()
            dismissLoadingDialog()
            Toast.makeText(this, "Error creating request body", Toast.LENGTH_SHORT).show()
            return
        }

        val request = JsonObjectRequest(
            Request.Method.PUT,
            LOGIN_URL,
            jsonBody,
            { response ->
                dismissLoadingDialog()
                try {
                    val isSuccess = response.getBoolean("isSuccess")
                    if (isSuccess) {
                        otpVerificationFragment(guardId)
                    } else {
                        guardID.error = "Wrong credentials"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Response parsing error", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                dismissLoadingDialog()
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(request)
    }

    private fun otpVerificationFragment(guardId: String) {
        val bundle = Bundle().apply {
            putString("Guard ID", guardId)
        }

        val otpVerificationFragment = OTPVerification().apply {
            arguments = bundle
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.otpVerificationFragment, otpVerificationFragment)
            addToBackStack(null)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Sending OTP...")
            show()
        }
    }

    private fun dismissLoadingDialog() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    companion object {
        private const val LOGIN_URL = "${Config.BASE_URL}/security/login"
    }
}
