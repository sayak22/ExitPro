package com.example.exitpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exitpro.data.api.ApiResponse
import com.example.exitpro.data.repository.ExitProRepository
import com.example.exitpro.viewmodel.state.ScanUiState
import com.example.exitpro.viewmodel.util.ApiResponseHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing student entry and exit scanning operations.
 * Maintains separate states for entry and exit flows to handle concurrent operations.
 */
class HomeViewModel(private val repository: ExitProRepository) : ViewModel() {

    private val _entryState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val entryState: StateFlow<ScanUiState> = _entryState.asStateFlow()

    private val _exitState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val exitState: StateFlow<ScanUiState> = _exitState.asStateFlow()

    fun processStudentEntry(rollNumber: Int) {
        processScanOperation(
            stateFlow = _entryState,
            operation = { repository.studentEntry(rollNumber) },
            alreadyScannedError = "Student is inside campus"
        )
    }

    fun processStudentExit(rollNumber: Int, destination: String) {
        processScanOperation(
            stateFlow = _exitState,
            operation = { repository.studentExit(rollNumber, destination) },
            alreadyScannedError = "Failed to process exit"
        )
    }

    fun resetEntryState() {
        _entryState.value = ScanUiState.Idle
    }

    fun resetExitState() {
        _exitState.value = ScanUiState.Idle
    }

    /**
     * Generic function to process scan operations (entry or exit).
     * Reduces code duplication by handling the common flow for both operations.
     */
    private fun processScanOperation(
        stateFlow: MutableStateFlow<ScanUiState>,
        operation: suspend () -> ApiResponse<com.example.exitpro.data.model.ScanResponse>,
        alreadyScannedError: String
    ) {
        viewModelScope.launch {
            stateFlow.value = ScanUiState.Loading

            val response = operation()
            
            when (response) {
                is ApiResponse.Success -> {
                    stateFlow.value = if (response.data.isSuccess) {
                        ScanUiState.Success(response.data)
                    } else {
                        ScanUiState.Error(alreadyScannedError)
                    }
                }
                is ApiResponse.Error -> {
                    stateFlow.value = ScanUiState.Error(response.message)
                }
                is ApiResponse.Exception -> {
                    stateFlow.value = ScanUiState.Error(
                        ApiResponseHandler.getExceptionMessage(response.exception)
                    )
                }
                is ApiResponse.Loading -> {}
            }
        }
    }
}