package com.example.sound

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.sound.databinding.ActivityMainBinding
import com.example.sound.logic.audio.AudioService
import java.io.IOException



class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var binding: ActivityMainBinding
    private var fileName: String = ""


    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val recordView = binding.root
        setContentView(recordView)

        // Record to the external cache directory for visibility
        fileName = "${externalCacheDir?.absolutePath}/audiometers.3gp"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        var startRecordingFlag = true
        binding.recordBtn.setOnClickListener{
            AudioService.onRecord(startRecordingFlag, fileName)
            binding.recordBtn.text = when (startRecordingFlag) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            startRecordingFlag = !startRecordingFlag
        }

        var startPlayingFlag = true
        binding.playBtn.setOnClickListener {
            AudioService.onPlay(startPlayingFlag, fileName)
            binding.playBtn.text = when (startPlayingFlag) {
                true -> "Stop playing"
                false -> "Start playing"
            }
            startPlayingFlag = !startPlayingFlag
        }
    }

    override fun onStop() {
        super.onStop()
        AudioService.onStop()
    }
}