package com.example.exitpro.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Request model for student exit (out scan) endpoint
 */
@Keep
@Parcelize
data class ExitRequest(
    @SerializedName("roll_number")
    val rollNumber: Int,
    @SerializedName("goingTo")
    val goingTo: String
) : Parcelable