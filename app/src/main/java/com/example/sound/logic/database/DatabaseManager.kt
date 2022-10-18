package com.example.sound.logic.database

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sound.MyApplication
import java.io.File

object DatabaseManager {
    // 目前使用绝对路径会报错
//    var DB_PATH = Environment.getExternalStorageDirectory().absolutePath +
//            File.separator + "mySoundApp" + File.separator+ "db" + File.separator
//    var DB_NAME = DB_PATH + "SoundData.db"
    var DB_NAME = "SoundData.db"

    private val MIGRATIONS = arrayOf(Migration1)
    private var applicationContext = MyApplication.context

    val db: AppDb by lazy {
        // 暂时不需要迁移
//        Room.databaseBuilder(application.applicationContext, AppDb::class.java, DB_NAME)
//            .addCallback(CreatedCallBack)
//            .addMigrations(*MIGRATIONS)
//            .build()

        // 目前使用绝对路径会报错
//        val dir = File(DB_PATH)
//        if (!dir.exists()) {
//            try {
//                dir.mkdirs()
//                println(dir.path)
//            } catch (exception: Exception){
//                println("mkdir error = ${exception.message}")
//            }
//        }

        Room.databaseBuilder(applicationContext, AppDb::class.java, DB_NAME)
            .addCallback(CreatedCallBack)
            .build()
    }

    private object CreatedCallBack : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            //在新装app时会调用，调用时机为数据库build()之后，数据库升级时不调用此函数
            MIGRATIONS.map {
                it.migrate(db)
            }
        }
    }

    private object Migration1 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 数据库的升级语句
            // database.execSQL("")
        }
    }
}

