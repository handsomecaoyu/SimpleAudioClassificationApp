package com.example.sound.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 除了Media.Audio中的信息，还需要额外一个数据库来保存音频的分类种类等信息
@Entity(tableName = "audioClass")
data class classEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var mediaId: Int = 0, // 在Media.Audio中的id
    var audioClass: String = "",
){
    constructor(mediaId: Int, audioClass: String) : this(){
        this.mediaId = mediaId
        this.audioClass = audioClass
    }
}
