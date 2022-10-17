package com.example.sound.logic.model

import com.example.sound.helps.AUDIO

data class Audio(
    val id: Long,
    val title: String,
    val uriString: String,
    val dateAddedTimeStamp: Long,
    val dateAddedString: String,
    val duration: String,
    val size: Int,
    val itemType: Int = AUDIO,
    var classResponse: ClassResponse = ClassResponse(),
    var isSelected: Boolean = false
)

