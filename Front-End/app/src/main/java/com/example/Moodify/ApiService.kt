package com.example.Moodify

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Request & Response Data Classes
data class SignUpRequest(val email: String, val password: String, val password1: String)
data class SignUpResponse(val status: String, val message: String)

data class PasswordVerificationRequest(val password: String)
data class PasswordVerificationResponse(val isValid: Boolean)

data class EmailAvailabilityRequest(val email: String)
data class EmailAvailabilityResponse(val isAvailable: Boolean)

data class EmailUpdateRequest(val email: String)
data class EmailUpdateResponse(val isUpdated: Boolean)

data class EmotionRequest(val emotion: String)
data class MusicResponse(val status: String, val message: String, val midi_file: String?)

interface ApiService {
    @POST("/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST("/verify-password")
    suspend fun verifyPassword(@Body request: PasswordVerificationRequest): Response<PasswordVerificationResponse>

    @POST("/check-email-availability")
    suspend fun checkEmailAvailability(@Body request: EmailAvailabilityRequest): Response<EmailAvailabilityResponse>

    @POST("/update-email")
    suspend fun updateEmail(@Body request: EmailUpdateRequest): Response<EmailUpdateResponse>

    @POST("/generate-music")
    fun generateMusic(@Body request: EmotionRequest): Call<MusicResponse>

    @POST("detect-emotion")
    fun detectEmotion(@Body imageBase64: String): Call<Map<String, String>>
}

