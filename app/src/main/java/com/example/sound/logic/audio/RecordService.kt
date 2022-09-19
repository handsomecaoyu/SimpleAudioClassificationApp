package com.example.sound.logic.audio

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
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// 实现跟录音相关的内容
class RecordService : Service() {

    private var amplitudeTimer = Timer()
    private var recorder: MediaRecorder? = null
    val APP_FOLDER_NAME: String = "mySoundApp"
    private val audioPath = "${Environment.DIRECTORY_MUSIC}/$APP_FOLDER_NAME/"
    var fileName: String? = null
    private val LOG_TAG = "AudioRecordTest"

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
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val displayName: String = "$formatted"

        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, audioPath)
        fileName = audioPath + displayName

        val uri = MyApplication.context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues) as Uri
        val outputFileDescriptor = MyApplication.context.contentResolver.openFileDescriptor(uri, "w")!!.fileDescriptor
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFileDescriptor)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }

        amplitudeTimer?.schedule(object : TimerTask() {
            override fun run() {
                val currentMaxAmplitude = recorder?.maxAmplitude
                if (currentMaxAmplitude!=null) {
                    EventBus.getDefault().post(MessageEvent(MessageType.updatemaxAmplitude).put(currentMaxAmplitude))
                }
            }
        }, 0, 100)

    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }
}