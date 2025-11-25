package com.example.exitpro.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data model representing a late student with automatic date/time parsing.
 * Parses the raw outTime string from API into individual date/time components.
 */

@Keep
data class LateStudent(
    @SerializedName("contact")
    val phoneNumber: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("goingTo")
    val destination: String = "",
    
    @SerializedName("outTime")
    private val outTimeRaw: String = "",
    
    @SerializedName("roll_number")
    val rollNumber: Int = 0
) {
    var year: Int = 0
        private set
    var month: Int = 0
        private set
    var day: Int = 0
        private set
    var hour: String = ""
        private set
    var minute: String = ""
        private set
    var second: String = ""
        private set

    init {
        parseOutTime()
    }

    /**
     * Parses outTimeRaw string into individual date/time components.
     * Expected format: "MMM dd yyyy HH:mm:ss" (e.g., "Nov 25 2025 12:30:45")
     * Silently fails if parsing errors occur, leaving default values.
     */
    private fun parseOutTime() {
        if (outTimeRaw.isBlank()) return
        
        try {
            val dateFormat = SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(outTimeRaw) ?: return

            Calendar.getInstance().apply {
                time = date
                year = get(Calendar.YEAR)
                month = get(Calendar.MONTH) + 1  // Calendar.MONTH is zero-based
                day = get(Calendar.DAY_OF_MONTH)
                hour = String.format("%02d", get(Calendar.HOUR_OF_DAY))
                minute = String.format("%02d", get(Calendar.MINUTE))
                second = String.format("%02d", get(Calendar.SECOND))
            }
        } catch (e: Exception) {
            // Parsing failed - keep default values
        }
    }
    
    /**
     * Returns formatted date string in DD-MM-YYYY format.
     */
    fun getFormattedDate(): String {
        return if (year > 0) "$day-$month-$year" else ""
    }
    
    /**
     * Returns formatted time string in HH:MM format.
     */
    fun getFormattedTime(): String {
        return if (hour.isNotEmpty()) "$hour:$minute" else ""
    }
}