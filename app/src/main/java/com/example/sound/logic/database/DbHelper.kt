package com.example.sound.logic.database

import android.content.Context
import android.os.Environment
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

class DbHelper {
    var db: AppDb
    var DB_PATH = Environment.getExternalStorageDirectory().absolutePath +
            File.separator + "mySoundApp" + File.separator+"db"+ File.separator
    var DB_NAME = DB_PATH +"mydb"

    private constructor(context: Context){
        //判断目录是否存在，不存在则创建该目录
        val dir = File(DB_PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        //允许在主线程中查询
        db = Room.databaseBuilder(context,AppDb::class.java, DB_NAME)
            .allowMainThreadQueries()
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
    }

    companion object {
        var context: Context?=null
        private var mInstance: DbHelper? = null
        fun getInstance(): DbHelper {
            if (DbHelper.mInstance == null) {
                synchronized(DbHelper::class.java) {
                    if (DbHelper.mInstance == null) {
                        DbHelper.mInstance = DbHelper(context!!)
                    }
                }
            }
            return DbHelper.mInstance!!
        }
    }
}