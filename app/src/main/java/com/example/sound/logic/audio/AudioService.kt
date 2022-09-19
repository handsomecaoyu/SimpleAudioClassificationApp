package com.example.sound.logic.audio

import android.content.ContentValues
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.sound.MyApplication
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object AudioService {
    private val LOG_TAG = "AudioRecordTest"

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    const val APP_FOLDER_NAME: String = "mySoundApp"
    private val audioPath = "${Environment.DIRECTORY_MUSIC}/$APP_FOLDER_NAME/"
    var fileName: String? = null

    fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    fun onPlay(start: Boolean) = if (start) {
        startPlaying(fileName)
    } else {
        stopPlaying()
    }

    fun onStop(){
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    private fun startPlaying(fileName: String?) {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    // 音频录制
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

    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }
}