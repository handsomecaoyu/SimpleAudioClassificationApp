package com.example.sound.ui.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.example.sound.MyApplication
import com.example.sound.databinding.FragmentHomeBinding
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import com.example.sound.logic.audio.AudioService
import com.example.sound.logic.audio.RecordService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


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
        EventBus.getDefault().unregister(this)
        _binding = null
    }


    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isgranted ->
            if (isgranted) {
                startRecord()
            }
        }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)



        binding.recordBtn.setOnClickListener{
            activity?.let {
                if (hasPermissions(activity as Context, arrayOf(Manifest.permission.RECORD_AUDIO))) {
                    if (startRecordingFlag){
                        startRecord()
                    } else {
                        stopRecord()
                    }
                } else {
                    permReqLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }

//        binding.recordBtn.setOnClickListener{
//            activity?.let {
//                if (hasPermissions(activity as Context, arrayOf(Manifest.permission.RECORD_AUDIO))) {
//
//                    AudioService.onRecord(startRecordingFlag)
//                    binding.recordBtn.text = when (startRecordingFlag) {
//                        true -> "Stop"
//                        false -> "Start"
//                    }
//                    startRecordingFlag = !startRecordingFlag
//                    binding.audioRecordView.recreate()
//
//                } else {
//                    permReqLauncher.launch(Manifest.permission.RECORD_AUDIO)
//                }
//            }
//        }


    }

//    override fun onStop() {
//        super.onStop()
//        AudioService.onStop()
//    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecord(){
        val intent = Intent(MyApplication.context, RecordService::class.java)
        MyApplication.context.startService(intent)
        binding.recordBtn.text = "Stop"
        startRecordingFlag = false
        binding.audioRecordView.recreate()
    }

    private fun stopRecord(){
        val intent = Intent(MyApplication.context, RecordService::class.java)
        MyApplication.context.stopService(intent)
        startRecordingFlag = true
        binding.recordBtn.text = "Start"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.updatemaxAmplitude -> {
                binding.audioRecordView.update(event.getInt())
            }
        }
    }

}