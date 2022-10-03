package com.example.sound.ui.audio

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sound.logic.Repository

class AudioViewModel : ViewModel(){
    // 获得存储在手机上的录制的音频
    fun getAudioName() = Repository.getAudioName()

    // 通过网络获得音频的分类
    private val audioPostOnline = MutableLiveData<String>()
    val audioClassLiveData = Transformations.switchMap(audioPostOnline){
        query -> Repository.getAudioClassOnline(query)
    }

    fun getClassificationResult(query: String){
        audioPostOnline.value = query
    }
}