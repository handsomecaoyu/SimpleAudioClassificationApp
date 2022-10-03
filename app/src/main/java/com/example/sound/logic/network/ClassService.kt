package com.example.sound.logic.network

import com.example.sound.logic.model.ClassResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ClassService {
    @GET("test_connection")
    fun getAudioClassOnline(@Query("query") query: String): Call<ClassResponse>
}