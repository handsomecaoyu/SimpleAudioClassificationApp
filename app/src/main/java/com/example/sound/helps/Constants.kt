package com.example.sound.helps


const val APP_FOLDER_NAME: String = "mySoundApp"

// 日志相关
const val RECORD_LOG_TAG = "AudioRecord"
const val HOME_LOG_TAG = "Home"
const val HISTORY_LOG_TAG = "History"

// 录音状态
const val START = 0
const val RECORDING = 1
const val STOP = 2
const val INFERENCE = 3

// 用于recyclerview显示的item类型
const val AUDIO = 0
const val DATE_ADDED = 1

// 结果的紧急程度
const val NORMAL = 0
const val WARNING = 1
const val ABNORMAL = 2

// 查看界面的音频状态
const val FINISHED = 0
const val PLAYING = 1
const val PAUSED = 2