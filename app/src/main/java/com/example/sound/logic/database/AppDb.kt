package com.example.sound.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sound.logic.dao.ClassDao
import com.example.sound.logic.model.classEntity

@Database(entities = [classEntity::class], version = 1, exportSchema = true)
abstract class AppDb: RoomDatabase() {
    abstract fun classDao(): ClassDao
}