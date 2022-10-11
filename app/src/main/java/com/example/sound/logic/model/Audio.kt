package com.example.sound.logic.model

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

