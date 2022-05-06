package com.example.sound.logic.audio

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.IOException

object AudioService {
    private val LOG_TAG = "AudioRecordTest"

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    fun onRecord(start: Boolean, fileName: String) = if (start) {
        startRecording(fileName)
    } else {
        stopRecording()
    }

    fun onPlay(start: Boolean, fileName: String) = if (start) {
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

    private fun startPlaying(fileName: String) {
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

    private fun startRecording(fileName: String) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

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