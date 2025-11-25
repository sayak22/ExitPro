package com.example.exitpro.viewmodel.state

import com.example.exitpro.data.model.LoginResponse

/**
 * UI states for login operation
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val data: LoginResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}