package com.example.sound.logic

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.liveData
import com.example.sound.MyApplication
import com.example.sound.logic.dao.AudioDao
import com.example.sound.logic.network.AudioClassNetwork
import com.example.sound.utils.URIPathHelper
import kotlinx.coroutines.Dispatchers
import java.io.File

object Repository {
    // 获得本地音频的名称
    fun getAudioName() = AudioDao.getAudioName()

    // 获得音频的分类结果
    fun getAudioClassOnline(audioUri: Uri) = liveData(Dispatchers.IO){
        val result = try {
            val uriPathHelper = URIPathHelper()
            val audioFile = File(uriPathHelper.getPath(MyApplication.context, audioUri))
            val classResponse = AudioClassNetwork.getAudioClassOnline(audioFile)
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