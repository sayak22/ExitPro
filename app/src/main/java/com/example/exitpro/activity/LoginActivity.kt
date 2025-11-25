package com.example.exitpro.activity

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.example.exitpro.R
import com.example.exitpro.data.api.RetrofitClient
import com.example.exitpro.data.repository.ExitProRepository
import com.example.exitpro.fragment.OTPVerification
import com.example.exitpro.viewmodel.LoginViewModel
import com.example.exitpro.viewmodel.ViewModelFactory
import com.example.exitpro.viewmodel.state.LoginUiState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var guardID: EditText
    private var loadingDialog: Dialog? = null
    
    // MVVM - ViewModel
    private val viewModel: LoginViewModel by lazy {
        val repository = ExitProRepository(RetrofitClient.apiService)
        val factory = ViewModelFactory(repository)
        factory.create(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        loginBtn = findViewById(R.id.login_button)
        guardID = findViewById(R.id.guard_id_input)

        // Set click listener for the login button
        loginBtn.setOnClickListener {
            val guardId = guardID.text.toString().trim()

            if (guardId.isNotEmpty()) {
                sendGuardID(guardId)
            } else {
                guardID.error = "Guard ID cannot be empty"
            }
        }
        
        // Observe ViewModel state
        observeLoginState()
    }

    /**
     * Send guard ID to server for login using ViewModel (MVVM pattern).
     */
    private fun sendGuardID(guardId: String) {
        viewModel.login(guardId)
    }
    
    /**
     * Observe login state from ViewModel and update UI accordingly.
     */
    private fun observeLoginState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            // Do nothing
                        }
                        is LoginUiState.Loading -> {
                            showLoadingDialog()
                        }
                        is LoginUiState.Success -> {
                            dismissLoadingDialog()
                            // Get the guard ID from the EditText to pass to OTP fragment
                            val guardId = guardID.text.toString().trim()
                            otpVerificationFragment(guardId)
                            viewModel.resetLoginState()
                        }
                        is LoginUiState.Error -> {
                            dismissLoadingDialog()
                            guardID.error = state.message
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetLoginState()
                        }
                    }
                }
            }
        }
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

    /**
     * Display a loading dialog with sending OTP message.
     */
    private fun showLoadingDialog() {
        loadingDialog = Dialog(this).apply {
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

}
