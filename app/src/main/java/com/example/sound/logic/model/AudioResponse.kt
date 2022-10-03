package com.example.sound.logic.model

data class AudioResponse(val audios: List<Audio>)
data class Audio(val id: Long, val title: String, val path: String, val timestamp: Int, val duration: Int, val size: Int)