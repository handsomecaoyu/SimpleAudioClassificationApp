package com.example.sound.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sound.logic.model.ClassEntity

@Dao
interface ClassDao{
    @Query("SELECT * FROM audioClass WHERE mediaId = :id")
    suspend fun getClassResult(id: Long): List<ClassEntity>

    @Query("SELECT * FROM audioClass")
    suspend fun getAllResult(): List<ClassEntity>

    @Insert
    suspend fun insert(vararg entity: ClassEntity): List<Long>

    @Delete
    suspend fun delete(vararg entities: ClassEntity): Int
}