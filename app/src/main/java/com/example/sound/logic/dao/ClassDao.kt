package com.example.sound.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sound.logic.model.classEntity

@Dao
interface ClassDao{
    @Query("SELECT * FROM audioClass WHERE 'mediaId' = (:ids)")
    suspend fun loadClassResult(ids: IntArray): List<classEntity>

    @Insert
    suspend fun insert(vararg entity: classEntity): List<Long>

    @Delete
    suspend fun delete(vararg entities: classEntity): Int
}