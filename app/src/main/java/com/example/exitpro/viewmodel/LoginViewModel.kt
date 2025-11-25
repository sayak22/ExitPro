package com.example.exitpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exitpro.data.api.ApiResponse
import com.example.exitpro.data.repository.ExitProRepository
import com.example.exitpro.viewmodel.state.LoginUiState
import com.example.exitpro.viewmodel.state.OTPUiState
import com.example.exitpro.viewmodel.util.ApiResponseHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication flow (login and OTP verification).
 * Manages two independent state flows for login and OTP verification.
 */
class LoginViewModel(private val repository: ExitProRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _otpState = MutableStateFlow<OTPUiState>(OTPUiState.Idle)
    val otpState: StateFlow<OTPUiState> = _otpState.asStateFlow()

    fun login(guardId: String) {
        if (guardId.isBlank()) {
            _loginState.value = LoginUiState.Error("Guard ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val response = repository.login(guardId)
            
            when (response) {
                is ApiResponse.Success -> {
                    _loginState.value = if (response.data.isSuccess) {
                        LoginUiState.Success(response.data)
                    } else {
                        LoginUiState.Error("Wrong credentials")
                    }
                }
                is ApiResponse.Error -> {
                    _loginState.value = LoginUiState.Error(response.message)
                }
                is ApiResponse.Exception -> {
                    _loginState.value = LoginUiState.Error(
                        ApiResponseHandler.getExceptionMessage(response.exception)
                    )
                }
                is ApiResponse.Loading -> {}
            }
        }
    }

    fun verifyOTP(guardId: String, otp: String) {
        if (otp.isBlank()) {
            _otpState.value = OTPUiState.Error("OTP cannot be empty")
            return
        }

        viewModelScope.launch {
            _otpState.value = OTPUiState.Loading

            val response = repository.verifyOTP(guardId, otp)
            
            when (response) {
                is ApiResponse.Success -> {
                    _otpState.value = if (response.data.isSuccess) {
                        OTPUiState.Success(response.data)
                    } else {
                        OTPUiState.Error("Wrong OTP")
                    }
                }
                is ApiResponse.Error -> {
                    _otpState.value = OTPUiState.Error(response.message)
                }
                is ApiResponse.Exception -> {
                    _otpState.value = OTPUiState.Error(
                        ApiResponseHandler.getExceptionMessage(response.exception)
                    )
                }
                is ApiResponse.Loading -> {}
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    fun resetOTPState() {
        _otpState.value = OTPUiState.Idle
    }
}