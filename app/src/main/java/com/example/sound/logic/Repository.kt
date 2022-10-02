package com.example.sound.logic

import com.example.sound.logic.dao.AudioDao

object Repository {
    fun getAudioName() = AudioDao.getAudioName()
}