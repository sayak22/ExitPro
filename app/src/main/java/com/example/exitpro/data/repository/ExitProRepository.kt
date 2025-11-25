package com.example.exitpro.data.repository

import com.example.exitpro.data.api.ApiResponse
import com.example.exitpro.data.api.ApiService
import com.example.exitpro.data.api.safeApiCall
import com.example.exitpro.data.model.*

/**
 * Repository for all API operations in the ExitPro application.
 * Acts as a single source of truth and abstraction layer between data sources and ViewModels.
 * All API calls are wrapped in ApiResponse for consistent error handling.
 */
class ExitProRepository(private val apiService: ApiService) {

    suspend fun login(guardId: String): ApiResponse<LoginResponse> {
        return safeApiCall {
            apiService.login(LoginRequest(guardId))
        }
    }

    suspend fun verifyOTP(guardId: String, otp: String): ApiResponse<OTPResponse> {
        return safeApiCall {
            apiService.verifyOTP(OTPRequest(guardId, otp))
        }
    }

    suspend fun studentEntry(rollNumber: Int): ApiResponse<ScanResponse> {
        return safeApiCall {
            apiService.studentEntry(rollNumber)
        }
    }

    suspend fun studentExit(rollNumber: Int, destination: String): ApiResponse<ScanResponse> {
        return safeApiCall {
            apiService.studentExit(ExitRequest(rollNumber, destination))
        }
    }

    suspend fun getLateStudents(): ApiResponse<List<LateStudent>> {
        return safeApiCall {
            apiService.getLateStudents()
        }
    }
}