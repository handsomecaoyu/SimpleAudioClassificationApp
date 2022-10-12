package com.example.sound.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.helps.WARNING

// 除了Media.Audio中的信息，还需要额外一个数据库来保存音频的分类种类等信息
@Entity(tableName = "audioClass")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var mediaId: Long = 0, // 在Media.Audio中的id
    var level: Int = WARNING,
    var audioClass: String = "",
    var status: String = ""
){
    constructor(mediaId: Long, classResult: ClassResponse) : this(){
        this.mediaId = mediaId
        this.audioClass = classResult.result
        this.level = classResult.level
        this.status = classResult.status
    }
}

