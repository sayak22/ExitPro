package com.example.exitpro.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Request model for OTP verification endpoint
 */
@Keep
@Parcelize
data class OTPRequest(
    @SerializedName("guardId")
    val guardId: String,
    @SerializedName("otp")
    val otp: String
) : Parcelable