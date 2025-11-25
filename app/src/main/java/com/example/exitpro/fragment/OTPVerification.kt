package com.example.exitpro.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exitpro.R
import com.example.exitpro.activity.HomeActivity
import com.example.exitpro.data.api.ApiResponse
import com.example.exitpro.data.api.RetrofitClient
import com.example.exitpro.data.repository.ExitProRepository
import kotlinx.coroutines.launch

class OTPVerification : Fragment() {
    // UI elements
    private lateinit var otpEditText: EditText
    private lateinit var verifyButton: Button
    private var loadingDialog: Dialog? = null
    private val repository by lazy { ExitProRepository(RetrofitClient.apiService) }
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
        showLoadingDialog()

        val otp = otpEditText.text.toString()
        val currentGuardId = guardId

        if (currentGuardId == null) {
            dismissLoadingDialog()
            showError("Guard ID not found")
            return
        }

        if (otp.isEmpty()) {
            dismissLoadingDialog()
            showError("Please enter OTP")
            return
        }

        lifecycleScope.launch {
            when (val response = repository.verifyOTP(currentGuardId, otp)) {
                is ApiResponse.Success -> {
                    dismissLoadingDialog()
                    if (response.data.isSuccess) {
                        val guardName = response.data.guardName ?: "Guard"
                        saveAccessToken(otp, guardName)
                        val intent = Intent(activity, HomeActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    } else {
                        showError("Wrong OTP")
                    }
                }
                is ApiResponse.Error -> {
                    dismissLoadingDialog()
                    showError("Error: ${response.message}")
                }
                is ApiResponse.Exception -> {
                    dismissLoadingDialog()
                    val errorMessage = when (response.exception) {
                        is java.net.UnknownHostException -> "No internet connection"
                        is java.net.SocketTimeoutException -> "Connection timeout"
                        else -> "Error: ${response.exception.message}"
                    }
                    showError(errorMessage)
                }
                is ApiResponse.Loading -> {
                    // Loading state already handled
                }
            }
        }
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
     * Display a loading dialog while verifying OTP.
     */
    private fun showLoadingDialog() {
        loadingDialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.loading_dialog)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    /**
     * Dismiss the loading dialog if currently showing.
     */
    private fun dismissLoadingDialog() {
        loadingDialog?.takeIf { it.isShowing }?.dismiss()
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

}
