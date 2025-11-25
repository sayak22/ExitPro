package com.example.exitpro.viewmodel.state

import com.example.exitpro.data.model.OTPResponse

/**
 * UI states for OTP verification
 */
sealed class OTPUiState {
    object Idle : OTPUiState()
    object Loading : OTPUiState()
    data class Success(val data: OTPResponse) : OTPUiState()
    data class Error(val message: String) : OTPUiState()
}