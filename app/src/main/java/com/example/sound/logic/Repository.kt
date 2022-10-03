package com.example.sound.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.example.sound.logic.dao.AudioDao
import com.example.sound.logic.network.AudioClassNetwork
import kotlinx.coroutines.Dispatchers

object Repository {
    // 获得本地音频的名称
    fun getAudioName() = AudioDao.getAudioName()

    // 获得音频的分类结果
    fun getAudioClassOnline(query: String) = liveData(Dispatchers.IO){
        val result = try {
            val classResponse = AudioClassNetwork.getAudioClassOnline(query)
            if (classResponse.status == "ok"){
                val audioClassResult = classResponse.result
                Result.success(audioClassResult)
            } else{
                Result.failure(RuntimeException("response status is ${classResponse.status}"))
            }
        } catch (e: Exception) {
            Log.e("AudioClassification", e.toString())
            Result.failure(e)
        }
        emit(result)
    }
}