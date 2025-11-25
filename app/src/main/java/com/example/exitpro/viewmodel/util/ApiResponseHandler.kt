package com.example.exitpro.viewmodel.util

import com.example.exitpro.data.api.ApiResponse
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object for handling API responses and converting exceptions to user-friendly messages.
 * Centralizes error handling logic to maintain consistency across ViewModels.
 */
object ApiResponseHandler {

    /**
     * Converts an ApiResponse.Exception to a user-friendly error message.
     */
    fun getExceptionMessage(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Connection timeout"
            else -> "An error occurred"
        }
    }

    /**
     * Handles ApiResponse and returns appropriate result.
     * @param T Success data type
     * @param onSuccess Callback for successful response with isSuccess flag
     * @param onError Callback for error response
     */
    inline fun <T> handleResponse(
        response: ApiResponse<T>,
        onSuccess: (T, Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        when (response) {
            is ApiResponse.Success -> {
                onSuccess(response.data, true)
            }
            is ApiResponse.Error -> {
                onError(response.message)
            }
            is ApiResponse.Exception -> {
                onError(getExceptionMessage(response.exception))
            }
            is ApiResponse.Loading -> {
                // Loading state handled externally
            }
        }
    }
}