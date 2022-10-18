package com.example.sound.logic.model

import com.example.sound.helps.AUDIO
import com.example.sound.helps.FINISHED

data class Audio(
    val id: Long,
    val title: String,
    val uriString: String,
    val dateAddedTimeStamp: Long,
    val dateAddedString: String,
    var duration: String,
    val size: Int,
    val itemType: Int = AUDIO,
    var classResponse: ClassResponse = ClassResponse(),
    var isSelected: Boolean = false,
    var isExpended: Boolean = false,
    var status: Int = FINISHED
)

