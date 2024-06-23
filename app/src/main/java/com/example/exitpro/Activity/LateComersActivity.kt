package com.example.exitpro.Activity

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.exitpro.Adapter.LateAdapter
import com.example.exitpro.Config.Config
import com.example.exitpro.GlobalVariables
import com.example.exitpro.Model.LateStudent
import com.example.exitpro.R
import com.example.exitpro.Utils.FingerprintAuthHelperUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.P)
class LateComersActivity : AppCompatActivity() {
    // Global variables and UI elements
    private lateinit var globalVariables: GlobalVariables
    private lateinit var lateList: ArrayList<LateStudent>
    private var progressDialog: ProgressDialog? = null
    private var fingerprintAuthHelperUtil: FingerprintAuthHelperUtil? = null
    private lateinit var lLateLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_late_comers)

        // Initialize global variables
        globalVariables = GlobalVariables()
        lateList = ArrayList()

        // Initialize UI elements
        lLateLayout = findViewById(R.id.lateLayout)

        // Initialize fingerprint authentication helper
        fingerprintAuthHelperUtil = FingerprintAuthHelperUtil(this, lLateLayout)

        // Show loading dialog
        showLoadingDialog()

        // Fetch the list of late students
        fetchLateStudents()
    }

    override fun onRestart() {
        super.onRestart()
        fingerprintAuthHelperUtil?.authenticate()
    }

    /**
     * Show the loading dialog.
     */
    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Loading...")
            show()
        }
    }

    /**
     * Dismiss the loading dialog if it is showing.
     */
    private fun dismissLoadingDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }

    /**
     * Fetch the list of late students from the backend.
     */
    private fun fetchLateStudents() {
        val queue = Volley.newRequestQueue(this)

        // Create and send JSON array request to fetch late students
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            LATE_URL,
            null,
            { response ->
                dismissLoadingDialog()
                handleResponse(response)
            },
            { error ->
                dismissLoadingDialog()
                handleError(error)
            })

        queue.add(jsonArrayRequest)
    }

    /**
     * Handle the response from the backend.
     *
     * @param response The JSON array response from the backend.
     */
    private fun handleResponse(response: JSONArray) {
        try {
            for (i in 0 until response.length()) {
                val jsonObject = response.getJSONObject(i)
                val student = parseLateStudent(jsonObject)
                student?.let { lateList.add(it) }
            }
            globalVariables.lateList = lateList
            updateUIWithLateList()
        } catch (e: JSONException) {
            Log.e("JSONError", "JSON parsing error", e)
        }
    }

    /**
     * Parse a JSON object into a LateStudent object.
     *
     * @param jsonObject The JSON object representing a late student.
     * @return The parsed LateStudent object, or null if parsing fails.
     */
    private fun parseLateStudent(jsonObject: JSONObject): LateStudent? {
        return try {
            val student = LateStudent().apply {
                phoneNumber = jsonObject.getString("contact")
                name = jsonObject.getString("name")
                rollNumber = jsonObject.getInt("roll_number")
                destination = jsonObject.getString("goingTo")

                val outTime = jsonObject.getString("outTime")
                val sdf = SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(outTime)

                date?.let {
                    val calendar = Calendar.getInstance().apply { time = it }
                    year = calendar[Calendar.YEAR]
                    month = calendar[Calendar.MONTH] + 1 // Month is zero-based
                    day = calendar[Calendar.DAY_OF_MONTH]
                    hour = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
                    minute = String.format("%02d", calendar[Calendar.MINUTE])
                    second = String.format("%02d", calendar[Calendar.SECOND])
                }
            }
            student
        } catch (e: JSONException) {
            Log.e("ParseError", "Failed to parse late student", e)
            null
        } catch (e: ParseException) {
            Log.e("ParseError", "Failed to parse date", e)
            null
        }
    }

    /**
     * Handle errors during network requests.
     *
     * @param error The error that occurred.
     */
    private fun handleError(error: VolleyError) {
        Toast.makeText(applicationContext, "ERROR - > $error", Toast.LENGTH_SHORT).show()
        Log.e("RequestError", error.toString())
    }

    /**
     * Update the UI with the list of late students.
     */
    private fun updateUIWithLateList() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@LateComersActivity)
            adapter = LateAdapter(this@LateComersActivity, lateList)
        }
    }

    companion object {
        // URL for fetching late students
        private const val LATE_URL: String = Config.BASE_URL + "/student/out/late"
    }
}
