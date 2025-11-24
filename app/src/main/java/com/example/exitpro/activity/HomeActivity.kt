package com.example.exitpro.activity

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.exitpro.GlobalVariables
import com.example.exitpro.R
import com.example.exitpro.config.Config
import com.example.exitpro.utils.CaptureActUtil
import com.example.exitpro.utils.FingerprintAuthHelperUtil
import com.example.exitpro.utils.PermissionUtil
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONException
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.P)
class HomeActivity : AppCompatActivity() {
    // UI elements
    private lateinit var btnOut: Button
    private lateinit var btnIn: Button
    private lateinit var btnLate: Button
    private lateinit var btnLogOut: Button
    private lateinit var guardNameView: TextView
    private lateinit var hHomeLayout: RelativeLayout

    // Variables
    private var scanNumber: Int = -1
    private var destination: String = ""
    private lateinit var globalVariables: GlobalVariables
    private var progressBar: ProgressBar? = null
    private var loadingDialog: Dialog? = null
    private lateinit var fingerprintAuthHelperUtil: FingerprintAuthHelperUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI elements
        btnOut = findViewById(R.id.btnOut)
        btnIn = findViewById(R.id.btnIn)
        btnLate = findViewById(R.id.btnLate)
        btnLogOut = findViewById(R.id.btnlogOut)
        hHomeLayout = findViewById(R.id.homeLayout)
        guardNameView = findViewById(R.id.guard_name_view)

        // Initialize global variables
        globalVariables = GlobalVariables()

        // Initialize fingerprint authentication helper
        fingerprintAuthHelperUtil = FingerprintAuthHelperUtil(this, hHomeLayout)
        fingerprintAuthHelperUtil.authenticate()

        // Set up button listeners
        setupButtonListeners()

        // Check if the user is logged in
        if (!isLoggedIn) {
            redirectToLoginActivity()
        }
        
        // Configure back button behavior
        setupBackPressHandler()
        
        // Request necessary runtime permissions
        requestRuntimePermissions()
    }

    override fun onRestart() {
        super.onRestart()
        fingerprintAuthHelperUtil.authenticate()
    }

    /**
     * Set up listeners for the buttons.
     */
    private fun setupButtonListeners() {
        // Logout button listener
        btnLogOut.setOnClickListener { logout() }

        // Scan out button listener
        btnOut.setOnClickListener {
            scanNumber = -1
            destination = ""
            startScan(outScan)
        }

        // Scan in button listener
        btnIn.setOnClickListener {
            scanNumber = -1
            startScan(inScan)
        }

        // Latecomers button listener
        btnLate.setOnClickListener {
            val intent = Intent(this, LateComersActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Start the barcode scanning process.
     *
     * @param scanLauncher The launcher for the scan activity.
     */
    private fun startScan(scanLauncher: ActivityResultLauncher<ScanOptions>) {
        val options = ScanOptions().apply {
            setOrientationLocked(false)
            setPrompt("Scan a barcode")
            setCameraId(0) // Use a specific camera of the device
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setCaptureActivity(CaptureActUtil::class.java)
        }
        scanLauncher.launch(options)
    }

    private val outScan = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        result.contents?.let {
            scanNumber = it.toInt()
            showDestinationDialog(scanNumber.toString())
        }
    }

    private val inScan = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        result.contents?.let {
            scanNumber = it.toInt()
            handleInScan()
        }
    }

    /**
     * Handle the scan-in process.
     */
    private fun handleInScan() {
        showLoadingDialog()
        val jsonRequest = JSONObject()
        val queue = Volley.newRequestQueue(this)

        // Create and send the JSON request for in scan
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT,
            "$inURL$scanNumber",
            jsonRequest,
            { response ->
                dismissLoadingDialog()
                handleInScanResponse(response)
            },
            { error ->
                dismissLoadingDialog()
                Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                error?.printStackTrace()
            })

        queue.add(jsonObjectRequest)
    }

    /**
     * Handle the response from the in-scan request.
     *
     * @param response The JSON response from the server.
     */
    private fun handleInScanResponse(response: JSONObject) {
        try {
            if (response.getBoolean("isSuccess")) {
                showSuccessDialog()
            } else {
                Toast.makeText(applicationContext, "STUDENT IS INSIDE CAMPUS!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            Log.e("JSONError", "Failed to parse in-scan response", e)
        }
    }

    /**
     * Request runtime permissions required by the app.
     * Includes camera for scanning and notifications.
     */
    private fun requestRuntimePermissions() {
        // Request camera permission if not granted
        if (!PermissionUtil.checkCameraPermission(this)) {
            PermissionUtil.requestCameraPermission(this)
        }
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtil.checkNotificationPermission(this)) {
                PermissionUtil.requestNotificationPermission(this)
            }
        }
    }
    
    /**
     * Configure back button behavior to minimize app instead of closing.
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }

    /**
     * Display a loading dialog while processing.
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

    /**
     * Check if the user is logged in.
     *
     * @return True if the user is logged in, false otherwise.
     */
    private val isLoggedIn: Boolean
        get() {
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            if (sharedPreferences.contains("guard_name")) {
                guardNameView.text = "Welcome, ${sharedPreferences.getString("guard_name", "")}!"
                return true
            }
            return false
        }

    /**
     * Log out the user and redirect to the login activity.
     */
    private fun logout() {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("otp")
            remove("guard_name")
            apply()
        }
        redirectToLoginActivity()
    }

    /**
     * Redirect to the login activity.
     */
    private fun redirectToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * Show a dialog to enter the destination after scanning a barcode.
     *
     * @param scannedBarcode The scanned barcode.
     */
    private fun showDestinationDialog(scannedBarcode: String) {
        val builder = AlertDialog.Builder(this).apply {
            setTitle("Enter Destination")
            val destinationInput = EditText(context)
            setView(destinationInput)

            setPositiveButton("OK") { dialog, _ ->
                destination = destinationInput.text.toString()
                if (destination.isNotEmpty()) {
                    sendOutScanRequest()
                } else {
                    Toast.makeText(applicationContext, "DESTINATION IS INVALID", Toast.LENGTH_SHORT).show()
                }
            }

            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        }

        builder.show()
    }

    /**
     * Send the out-scan request to the server.
     */
    private fun sendOutScanRequest() {
        showLoadingDialog()
        val jsonObject = JSONObject().apply {
            put("roll_number", scanNumber)
            put("goingTo", destination)
        }

        val queue = Volley.newRequestQueue(this)
        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, outURL, jsonObject,
            { response ->
                dismissLoadingDialog()
                handleOutScanResponse(response)
            },
            { error ->
                dismissLoadingDialog()
                Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                error?.printStackTrace()
            })

        queue.add(jsonRequest)
    }

    /**
     * Handle the response from the out-scan request.
     *
     * @param response The JSON response from the server.
     */
    private fun handleOutScanResponse(response: JSONObject) {
        try {
            if (response.getBoolean("isSuccess")) {
                showSuccessDialog()
            } else {
                Toast.makeText(applicationContext, "STUDENT ALREADY OUT!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            Log.e("JSONError", "Failed to parse out-scan response", e)
        }
    }

    /**
     * Show a success dialog for a short duration.
     */
    private fun showSuccessDialog() {
        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.success_dialog)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }

        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 2000)
    }

    /**
     * Handle permission request results from the user.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PermissionUtil.REQUEST_CAMERA_PERMISSION -> {
                PermissionUtil.onRequestPermissionsResult(
                    requestCode, permissions, grantResults,
                    onGranted = {
                        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                    },
                    onDenied = {
                        Toast.makeText(this, "Camera permission is required for scanning QR codes", Toast.LENGTH_LONG).show()
                    }
                )
            }
            PermissionUtil.REQUEST_NOTIFICATION_PERMISSION -> {
                PermissionUtil.onRequestPermissionsResult(
                    requestCode, permissions, grantResults,
                    onGranted = {
                        // Notification permission granted
                    },
                    onDenied = {
                        Toast.makeText(this, "Notification permission denied. You may not receive important updates.", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    companion object {
        // URLs for API requests
        private const val outURL: String = "${Config.BASE_URL}/student/gate/exit"
        private const val inURL: String = "${Config.BASE_URL}/student/gate/entry/"
    }
}