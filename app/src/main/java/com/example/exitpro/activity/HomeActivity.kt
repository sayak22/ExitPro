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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.example.exitpro.GlobalVariables
import com.example.exitpro.R
import com.example.exitpro.data.api.RetrofitClient
import com.example.exitpro.data.repository.ExitProRepository
import com.example.exitpro.utils.CaptureActUtil
import com.example.exitpro.utils.FingerprintAuthHelperUtil
import com.example.exitpro.utils.PermissionUtil
import com.example.exitpro.viewmodel.HomeViewModel
import com.example.exitpro.viewmodel.ViewModelFactory
import com.example.exitpro.viewmodel.state.ScanUiState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

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
    
    // MVVM - ViewModel
    private val viewModel: HomeViewModel by lazy {
        val repository = ExitProRepository(RetrofitClient.apiService)
        val factory = ViewModelFactory(repository)
        factory.create(HomeViewModel::class.java)
    }

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
        
        // Observe ViewModel states
        observeViewModelStates()
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
     * Handle the scan-in process using ViewModel (MVVM pattern).
     */
    private fun handleInScan() {
        viewModel.processStudentEntry(scanNumber)
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
     * Send the out-scan request to the server using ViewModel (MVVM pattern).
     */
    private fun sendOutScanRequest() {
        viewModel.processStudentExit(scanNumber, destination)
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
     * Observe ViewModel state changes and update UI accordingly (MVVM pattern).
     */
    private fun observeViewModelStates() {
        // Observe student entry state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entryState.collect { state ->
                    when (state) {
                        is ScanUiState.Idle -> {
                            // Do nothing
                        }
                        is ScanUiState.Loading -> {
                            showLoadingDialog()
                        }
                        is ScanUiState.Success -> {
                            dismissLoadingDialog()
                            showSuccessDialog()
                            viewModel.resetEntryState()
                        }
                        is ScanUiState.Error -> {
                            dismissLoadingDialog()
                            val message = if (state.message == "Student is inside campus") {
                                "STUDENT IS INSIDE CAMPUS!"
                            } else {
                                state.message
                            }
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                            viewModel.resetEntryState()
                        }
                    }
                }
            }
        }

        // Observe student exit state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exitState.collect { state ->
                    when (state) {
                        is ScanUiState.Idle -> {
                            // Do nothing
                        }
                        is ScanUiState.Loading -> {
                            showLoadingDialog()
                        }
                        is ScanUiState.Success -> {
                            dismissLoadingDialog()
                            showSuccessDialog()
                            viewModel.resetExitState()
                        }
                        is ScanUiState.Error -> {
                            dismissLoadingDialog()
                            Toast.makeText(applicationContext, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetExitState()
                        }
                    }
                }
            }
        }
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

}