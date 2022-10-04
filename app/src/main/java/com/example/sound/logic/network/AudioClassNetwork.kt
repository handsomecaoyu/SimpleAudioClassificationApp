package com.example.sound.logic.network

import android.util.Log
import com.example.sound.logic.model.ClassResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object AudioClassNetwork {
    private val classService = ServiceCreator.create<ClassService>()

    suspend fun getAudioClassOnline(audio: File): ClassResponse {
        val postAudio = MultipartBody.Part
            .createFormData(
                "postAudio",
                audio.name,
                RequestBody.create(MediaType.parse("audio/*"), audio)
            )
        return classService.getAudioClassOnline(postAudio).await()
    }

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null)
                        continuation.resume(body)
                    else
                        continuation.resumeWithException(RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }

    }
}
