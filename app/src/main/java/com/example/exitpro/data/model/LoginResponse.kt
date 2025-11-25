package com.example.exitpro.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Response model for login endpoint
 */
@Keep
@Parcelize
data class LoginResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean
) : Parcelable