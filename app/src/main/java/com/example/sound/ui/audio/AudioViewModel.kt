package com.example.sound.ui.audio

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sound.logic.Repository
import java.io.File

class AudioViewModel : ViewModel(){
    // 获得存储在手机上的录制的音频
    fun getAudioName() = Repository.getAudioName()

    // 通过网络获得音频的分类
    private val audioPostOnline = MutableLiveData<Uri>()
    val audioClassLiveData = Transformations.switchMap(audioPostOnline){
        audioUri -> Repository.getAudioClassOnline(audioUri)
    }

    fun getClassificationResult(audioUri: Uri){
        audioPostOnline.value = audioUri
    }
}