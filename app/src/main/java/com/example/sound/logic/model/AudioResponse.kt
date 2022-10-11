package com.example.sound.logic.model

import com.example.sound.helps.AUDIO
import com.example.sound.helps.DATE_ADDED

data class AudioResponse(val audios: List<Audio>)
data class Audio(
    val id: Long,
    val title: String,
    val path: String,
    val dateAddedTimeStamp: Long,
    val dateAddedString: String,
    val duration: String,
    val size: Int,
    val itemType: Int,
    var classResult: String = "未录入"
)

