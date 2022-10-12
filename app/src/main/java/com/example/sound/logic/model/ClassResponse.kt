package com.example.sound.logic.model

import com.example.sound.helps.WARNING

data class ClassResponse(
    val status: String = "",
    val result: String = "",
    val level: Int = WARNING
)
