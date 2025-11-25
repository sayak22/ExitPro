package com.example.exitpro.activity

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exitpro.GlobalVariables
import com.example.exitpro.R
import com.example.exitpro.adapter.LateAdapter
import com.example.exitpro.data.api.RetrofitClient
import com.example.exitpro.data.model.LateStudent
import com.example.exitpro.data.repository.ExitProRepository
import com.example.exitpro.utils.FingerprintAuthHelperUtil
import com.example.exitpro.utils.PermissionUtil
import com.example.exitpro.viewmodel.LateComersViewModel
import com.example.exitpro.viewmodel.ViewModelFactory
import com.example.exitpro.viewmodel.state.LateStudentsUiState
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.P)
class LateComersActivity : AppCompatActivity() {

    // Declare global variables and UI elements
    private lateinit var globalVariables: GlobalVariables
    private lateinit var lateList: ArrayList<LateStudent>
    private var loadingDialog: Dialog? = null
    private var fingerprintAuthHelperUtil: FingerprintAuthHelperUtil? = null
    private lateinit var lLateLayout: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var lateAdapter: LateAdapter
    
    // MVVM - ViewModel
    private val viewModel: LateComersViewModel by lazy {
        val repository = ExitProRepository(RetrofitClient.apiService)
        val factory = ViewModelFactory(repository)
        factory.create(LateComersViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_late_comers)

        // Initialize global variables and UI elements
        globalVariables = GlobalVariables()
        lateList = ArrayList()
        lLateLayout = findViewById(R.id.lateLayout)

        // Initialize fingerprint authentication helper
        fingerprintAuthHelperUtil = FingerprintAuthHelperUtil(this, lLateLayout)

        // Initialize RecyclerView and its adapter
        lateAdapter = LateAdapter(this, lateList)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = lateAdapter

        requestCallPhonePermission()
        
        // Fetch the list of late students
        fetchLateStudents()

        // Initialize and set up the search view
        searchView = findViewById(R.id.student_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Called when the user submits the query
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Called when the user changes the query text
                viewModel.searchStudents(newText)
                return true
            }
        })
        
        // Observe ViewModel state
        observeLateStudentsState()
    }


    override fun onRestart() {
        super.onRestart()
        // Authenticate using fingerprint when the activity restarts
        fingerprintAuthHelperUtil?.authenticate()
    }

    /**
     * Display a loading dialog while fetching data.
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
     * Fetch the list of late students using ViewModel (MVVM pattern).
     */
    private fun fetchLateStudents() {
        viewModel.fetchLateStudents()
    }
    
    /**
     * Observe late students state from ViewModel and update UI accordingly.
     */
    private fun observeLateStudentsState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lateStudentsState.collect { state ->
                    when (state) {
                        is LateStudentsUiState.Idle -> {
                            // Do nothing
                        }
                        is LateStudentsUiState.Loading -> {
                            showLoadingDialog()
                        }
                        is LateStudentsUiState.Success -> {
                            dismissLoadingDialog()
                            lateList.clear()
                            lateList.addAll(state.students)
                            globalVariables.lateList = lateList
                            lateAdapter.notifyDataSetChanged()
                        }
                        is LateStudentsUiState.Empty -> {
                            dismissLoadingDialog()
                            Toast.makeText(this@LateComersActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        is LateStudentsUiState.Error -> {
                            dismissLoadingDialog()
                            Toast.makeText(this@LateComersActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Request permission to make phone calls.
     */
    private fun requestCallPhonePermission() {
        if (!PermissionUtil.checkCallPhonePermission(this)) {
            PermissionUtil.requestCallPhonePermission(this)
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
            PermissionUtil.REQUEST_CALL_PHONE_PERMISSION -> {
                PermissionUtil.onRequestPermissionsResult(
                    requestCode, permissions, grantResults,
                    onGranted = {
                        Toast.makeText(this, "Phone permission granted", Toast.LENGTH_SHORT).show()
                    },
                    onDenied = {
                        Toast.makeText(this, "Phone permission is required to call students", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
    
}
