package com.example.sound.logic.network

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object AudioClassNetwork {
    private val classService = ServiceCreator.create<ClassService>()

    suspend fun getAudioClassOnline(query: String) = classService.getAudioClassOnline(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    Log.v("Myresponse", "onResponse")
                    if (body != null)
                        continuation.resume(body)
                    else
                        continuation.resumeWithException(RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    Log.v("Myresponse", "onFailure")
                    continuation.resumeWithException(t)
                }
            })
        }

    }
}