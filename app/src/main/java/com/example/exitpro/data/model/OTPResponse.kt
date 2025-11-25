package com.example.exitpro.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Response model for OTP verification endpoint
 */
@Keep
@Parcelize
data class OTPResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,
    @SerializedName("guardName")
    val guardName: String? = null
) : Parcelable