package com.example.sound.logic.network

import com.example.sound.logic.model.ClassResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ClassService {
    @Multipart
    @POST("audio_classify")
    fun getAudioClassOnline(@Part audio: MultipartBody.Part): Call<ClassResponse>
}