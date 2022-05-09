package com.example.sound.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.sound.databinding.FragmentHomeBinding
import com.example.sound.logic.audio.AudioService


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var startRecordingFlag = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isgranted ->
            if (isgranted) {
                AudioService.onRecord(startRecordingFlag)
                binding.recordBtn.text = when (startRecordingFlag) {
                    true -> "Stop recording"
                    false -> "Start recording"
                }
                startRecordingFlag = !startRecordingFlag
            }
        }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recordBtn.setOnClickListener{
            activity?.let {
                if (hasPermissions(activity as Context, arrayOf(Manifest.permission.RECORD_AUDIO))) {

                    AudioService.onRecord(startRecordingFlag)
                    binding.recordBtn.text = when (startRecordingFlag) {
                        true -> "Stop"
                        false -> "Start"
                    }
                    startRecordingFlag = !startRecordingFlag

                } else {
                    permReqLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
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

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

}