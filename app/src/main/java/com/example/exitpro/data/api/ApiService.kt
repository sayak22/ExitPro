package com.example.exitpro.data.api

import com.example.exitpro.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface defining all API endpoints
 */
interface ApiService {
    
    /**
     * Login endpoint - sends guard ID and receives success status
     * PUT /security/login
     */
    @PUT("security/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    /**
     * OTP verification endpoint - validates OTP for guard login
     * POST /security/otpMatch
     */
    @POST("security/otpMatch")
    suspend fun verifyOTP(@Body request: OTPRequest): Response<OTPResponse>
    
    /**
     * Student entry (in scan) endpoint - marks student as entered
     * PUT /student/gate/entry/{rollNumber}
     */
    @PUT("student/gate/entry/{rollNumber}")
    suspend fun studentEntry(@Path("rollNumber") rollNumber: Int): Response<ScanResponse>
    
    /**
     * Student exit (out scan) endpoint - marks student as exited with destination
     * POST /student/gate/exit
     */
    @POST("student/gate/exit")
    suspend fun studentExit(@Body request: ExitRequest): Response<ScanResponse>
    
    /**
     * Get late students endpoint - retrieves list of students who are late
     * GET /student/out/late
     */
    @GET("student/out/late")
    suspend fun getLateStudents(): Response<List<LateStudent>>
}