package com.example.sound

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.sound.databinding.ActivityMainBinding
import com.example.sound.logic.audio.AudioService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var binding: ActivityMainBinding

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



    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val recordView = binding.root
        setContentView(recordView)

        // Record to the external cache directory for visibility
        // fileName = "${externalCacheDir?.absolutePath}/audiometers.3gp"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        var startRecordingFlag = true
        binding.recordBtn.setOnClickListener{
            AudioService.onRecord(startRecordingFlag)
            binding.recordBtn.text = when (startRecordingFlag) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            startRecordingFlag = !startRecordingFlag
        }

        var startPlayingFlag = true
        binding.playBtn.setOnClickListener {
            AudioService.onPlay(startPlayingFlag)
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