package com.example.Moodify

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://127.0.0.1:5000/"  // Update this with your Flask server URL

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())  // Convert JSON automatically
            .build()
    }
}
