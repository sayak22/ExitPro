package com.example.exitpro.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.exitpro.R
import com.example.exitpro.Utils.FingerprintAuthHelperUtil

class MainActivity : AppCompatActivity() {

    // Declare FingerprintAuthHelperUtil object
    private lateinit var fingerprintAuthHelperUtil: FingerprintAuthHelperUtil
    private lateinit var mMainLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize layout elements
        mMainLayout = findViewById(R.id.mainLayout)

        // Initialize FingerprintAuthHelperUtil
        fingerprintAuthHelperUtil = FingerprintAuthHelperUtil(this@MainActivity, mMainLayout)

        // Check if the user is logged in
        if (!isLoggedIn) {
            // If user is not logged in, redirect to login activity
            redirectToLoginActivity()
        } else {
            // Otherwise, redirect to home activity
            redirectToHomeActivity()
        }
    }

    override fun onRestart() {
        super.onRestart()

        // Authenticate using fingerprint
        fingerprintAuthHelperUtil.authenticate()

        // Check if the user is logged in
        if (!isLoggedIn) {
            // If user is not logged in, redirect to login activity
            redirectToLoginActivity()
        } else {
            // Otherwise, redirect to home activity
            redirectToHomeActivity()
        }
    }

    // Check if the user is logged in by verifying the presence of an access token
    private val isLoggedIn: Boolean
        get() {
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            return sharedPreferences.contains("access_token")
        }

    // Redirect to the LoginActivity
    private fun redirectToLoginActivity() {
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            finish() // Finish MainActivity so the user cannot navigate back to it
        }
    }

    // Redirect to the HomeActivity
    private fun redirectToHomeActivity() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
        }
    }
}
