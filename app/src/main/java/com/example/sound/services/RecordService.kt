package com.example.sound.services

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import com.example.sound.MyApplication
import com.example.sound.helps.APP_FOLDER_NAME
import com.example.sound.helps.RECORD_LOG_TAG
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// 实现跟录音相关的内容
class RecordService : Service() {

    private var amplitudeTimer = Timer() // 幅值计算的定时器
    private var durationTimer = Timer() // 录音计时器
    private var duration = 0 // 录音时长, ms

    private var recorder: MediaRecorder? = null
    private val audioPath = "${Environment.DIRECTORY_MUSIC}/$APP_FOLDER_NAME/"
    // var fileName: String? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    // 启动执行
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun startRecording(mimeType: String ="audio/mpeg") {
        val contentValues = ContentValues()
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")
        val formatted = current.format(formatter)
        val displayName: String = "$formatted"

        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, audioPath)
        // fileName = audioPath + displayName

        val uri = MyApplication.context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues) as Uri

        // 传递uri
        EventBus.getDefault().post(uri.toString()?.let { MessageEvent(MessageType.RecordUri).put(it) })

        val outputFileDescriptor = MyApplication.context.contentResolver.openFileDescriptor(uri, "w")!!.fileDescriptor
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFileDescriptor)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(RECORD_LOG_TAG, "prepare() failed")
            }

            start()
        }

        // 录音界面的波形相关
        amplitudeTimer.schedule(object : TimerTask() {
            override fun run() {
                var currentMaxAmplitude = recorder?.maxAmplitude
                if (currentMaxAmplitude!=null) {
                    EventBus.getDefault().post(MessageEvent(MessageType.UpdatemaxAmplitude).put(currentMaxAmplitude))
                }
            }
        }, 0, 50)

        // 录音界面的时钟相关
        durationTimer.schedule(object : TimerTask(){
            override fun run() {
                duration += 50
                EventBus.getDefault().post(MessageEvent(MessageType.UpdateDuration).put(duration))
            }
        }, 0, 50)
    }

    private fun stopRecording() {
        duration = 0
        durationTimer.cancel()
        amplitudeTimer.cancel()
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
    }

}