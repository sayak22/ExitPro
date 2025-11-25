package com.example.exitpro.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Request model for login endpoint
 */
@Keep
@Parcelize
data class LoginRequest(
    @SerializedName("guardId")
    val guardId: String
) : Parcelable