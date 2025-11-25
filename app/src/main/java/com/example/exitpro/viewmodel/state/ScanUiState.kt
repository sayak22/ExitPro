package com.example.exitpro.viewmodel.state

import com.example.exitpro.data.model.ScanResponse

/**
 * UI states for student entry/exit scan operations
 */
sealed class ScanUiState {
    object Idle : ScanUiState()
    object Loading : ScanUiState()
    data class Success(val data: ScanResponse) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}