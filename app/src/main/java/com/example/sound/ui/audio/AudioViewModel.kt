package com.example.sound.ui.audio

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sound.logic.Repository
import com.example.sound.logic.database.DatabaseManager
import com.example.sound.logic.model.ClassResponse
import kotlinx.coroutines.*
import java.io.File

class AudioViewModel : ViewModel(){
    private val viewJob = Job()
    private val viewScope = CoroutineScope(viewJob)
    // 获得存储在手机上的录制的音频
    fun getAudioInfo() = Repository.getAudioInfo()

    // 通过网络获得音频的分类
    private val audioPostOnline = MutableLiveData<Uri>()
    val audioClassLiveData = Transformations.switchMap(audioPostOnline){
        audioUri -> Repository.getAudioClassOnline(audioUri)
    }

    fun getClassificationResult(audioUri: Uri){
        audioPostOnline.value = audioUri
    }

    // 从本地数据库获得音频的分类
    fun getAudioClassFromDB(id: Long): ClassResponse {
        return runBlocking {
            val classResultEntities = withContext(Dispatchers.IO) {
                Repository.getAudioClassFromDB(id)
            }
            if (classResultEntities.isNotEmpty()){
                val classResultEntity = classResultEntities[0]
                classResultEntity.let { ClassResponse(it.status, it.audioClass, it.level) }
            }
            else
                ClassResponse(result="未录入")
        }
    }


    override fun onCleared() {
        viewJob.cancel()
    }
}