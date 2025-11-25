package com.example.exitpro.viewmodel.state

import com.example.exitpro.data.model.LateStudent

/**
 * UI states for late students list operations
 */
sealed class LateStudentsUiState {
    object Idle : LateStudentsUiState()
    object Loading : LateStudentsUiState()
    data class Success(val students: List<LateStudent>) : LateStudentsUiState()
    data class Empty(val message: String) : LateStudentsUiState()
    data class Error(val message: String) : LateStudentsUiState()
}