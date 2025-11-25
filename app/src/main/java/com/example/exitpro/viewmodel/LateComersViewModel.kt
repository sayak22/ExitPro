package com.example.exitpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exitpro.data.api.ApiResponse
import com.example.exitpro.data.model.LateStudent
import com.example.exitpro.data.repository.ExitProRepository
import com.example.exitpro.viewmodel.state.LateStudentsUiState
import com.example.exitpro.viewmodel.util.ApiResponseHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing late students list with search functionality.
 * Maintains local cache of all students for efficient filtering.
 */
class LateComersViewModel(private val repository: ExitProRepository) : ViewModel() {

    private val _lateStudentsState = MutableStateFlow<LateStudentsUiState>(LateStudentsUiState.Idle)
    val lateStudentsState: StateFlow<LateStudentsUiState> = _lateStudentsState.asStateFlow()

    private var allLateStudents: List<LateStudent> = emptyList()

    fun fetchLateStudents() {
        viewModelScope.launch {
            _lateStudentsState.value = LateStudentsUiState.Loading

            val response = repository.getLateStudents()
            
            when (response) {
                is ApiResponse.Success -> {
                    allLateStudents = response.data
                    _lateStudentsState.value = LateStudentsUiState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _lateStudentsState.value = LateStudentsUiState.Error(response.message)
                }
                is ApiResponse.Exception -> {
                    _lateStudentsState.value = LateStudentsUiState.Error(
                        ApiResponseHandler.getExceptionMessage(response.exception)
                    )
                }
                is ApiResponse.Loading -> {}
            }
        }
    }

    fun searchStudents(query: String?) {
        if (query.isNullOrBlank()) {
            _lateStudentsState.value = LateStudentsUiState.Success(allLateStudents)
            return
        }

        val filteredStudents = filterStudentsByName(query)
        
        _lateStudentsState.value = if (filteredStudents.isEmpty()) {
            LateStudentsUiState.Empty("No students found")
        } else {
            LateStudentsUiState.Success(filteredStudents)
        }
    }

    fun resetState() {
        _lateStudentsState.value = LateStudentsUiState.Idle
    }

    /**
     * Filters students by name using case-insensitive search.
     */
    private fun filterStudentsByName(query: String): List<LateStudent> {
        val lowercaseQuery = query.lowercase()
        return allLateStudents.filter { student ->
            student.name?.lowercase()?.contains(lowercaseQuery) == true
        }
    }
}