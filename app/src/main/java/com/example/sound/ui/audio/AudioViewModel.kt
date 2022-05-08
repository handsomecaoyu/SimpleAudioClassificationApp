package com.example.sound.ui.audio

import androidx.lifecycle.ViewModel
import com.example.sound.logic.audio.Repository

class AudioViewModel : ViewModel(){
    fun getAudioName() = Repository.getAudioName()
}