package com.example.sound

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //用于添加当前Activity到activities的List中：用于可以随时随地的退出程序
        ActivityCollector.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        //用于删除当前Activity到activities的List中：用于可以随时随地的退出程序
        ActivityCollector.removeActivity(this)
    }
}
