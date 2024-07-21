package com.example.exitpro.Activity

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

    // Declare global variables and UI elements
    private lateinit var globalVariables: GlobalVariables
    private lateinit var lateList: ArrayList<LateStudent>
    private var progressDialog: ProgressDialog? = null
    private var fingerprintAuthHelperUtil: FingerprintAuthHelperUtil? = null
    private lateinit var lLateLayout: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var lateAdapter: LateAdapter

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

        // Show loading dialog and fetch the list of late students
        showLoadingDialog()
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
                filterList(newText)
                return true
            }
        })
    }

    /**
     * Filter the list of students based on the search text.
     *
     * @param text The text to filter the list by.
     */
    private fun filterList(text: String?) {
        val filteredList: MutableList<LateStudent> = mutableListOf()

        // Filter the lateList based on the search text
        text?.let { // this block only executes if text is not null
            for (item in lateList) {
                if (item.name?.lowercase()?.contains(text.lowercase()) == true) {
                    filteredList.add(item)
                }
            }
        }

            // Update the adapter with the filtered list (even if it is empty)
            lateAdapter.setFilteredList(filteredList)

        // Show a toast if no students match the search text
        if (filteredList.isEmpty())
            Toast.makeText(this, "Student not found.", Toast.LENGTH_SHORT).show()


    }

    override fun onRestart() {
        super.onRestart()
        // Authenticate using fingerprint when the activity restarts
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
            lateAdapter.notifyDataSetChanged()  // Notify the adapter of data changes
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

    companion object {
        // URL for fetching late students
        private const val LATE_URL: String = Config.BASE_URL + "/student/out/late"
    }
}
