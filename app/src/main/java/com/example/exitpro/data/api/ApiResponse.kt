package com.example.exitpro.data.api

/**
 * Sealed class representing different states of API responses.
 * Provides type-safe handling of success, error, and loading states.
 */
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()
    data class Exception(val exception: Throwable) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

/**
 * Executes API calls safely with automatic error handling.
 * Wraps Retrofit responses in ApiResponse sealed class for consistent error handling.
 * 
 * @param T Response data type
 * @param apiCall Suspend function that makes the actual API call
 * @return ApiResponse wrapping the result (Success, Error, or Exception)
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): ApiResponse<T> {
    return try {
        val response = apiCall()
        
        if (response.isSuccessful) {
            response.body()?.let { body ->
                ApiResponse.Success(body)
            } ?: ApiResponse.Error("Empty response body", response.code())
        } else {
            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
            ApiResponse.Error(errorMessage, response.code())
        }
    } catch (e: java.net.UnknownHostException) {
        ApiResponse.Exception(e)
    } catch (e: java.net.SocketTimeoutException) {
        ApiResponse.Exception(e)
    } catch (e: Exception) {
        ApiResponse.Exception(e)
    }
}