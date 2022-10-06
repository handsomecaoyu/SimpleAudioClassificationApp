package com.example.sound.ui.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.databinding.FragmentHomeBinding
import com.example.sound.helps.INFERENCE
import com.example.sound.helps.STOP
import com.example.sound.helps.RECORDING
import com.example.sound.helps.START
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import com.example.sound.services.RecordService
import com.example.sound.ui.audio.AudioAdapter
import com.example.sound.ui.audio.AudioViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var status = START
    private var recordingUriString : String? = null
    private var audioClassDisplay: String = ""
    private val viewModel by lazy { ViewModelProvider(this).get(AudioViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        _binding = null
    }
    

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        // 一开始隐藏取消按钮、声音波形和识别结果
        binding.audioRecordView.visibility = View.INVISIBLE
        binding.cancelBtn.visibility = View.INVISIBLE
        binding.resultDisplay.visibility = View.INVISIBLE

        // 设置录音按键动作
        binding.recordBtn.setOnClickListener{
            if (hasPermissions(activity as Context, arrayOf(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET))) {
                when (status) {
                    START -> startRecord()
                    RECORDING -> stopRecord()
                    STOP -> inferenceRecord()
                    INFERENCE -> prepareRecord()
                }
            } else {
                permReqLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET))
            }
        }

        // 设置取消按键动作
        binding.cancelBtn.setOnClickListener {
            deleteRecording()
            prepareRecord()
        }
    }

    // 获取录音和联网权限
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                startRecord()
            }
        }

    // 判断有无权限
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // 开始录音
    private fun startRecord(){
        // 开启录音的service
        val intent = Intent(MyApplication.context, RecordService::class.java)
        MyApplication.context.startService(intent)
        status = RECORDING
        uiChange(status)
    }

    // 停止录音
    private fun stopRecord(){
        val intent = Intent(MyApplication.context, RecordService::class.java)
        MyApplication.context.stopService(intent)
        status = STOP
        uiChange(status)
    }

    // 音频推理
    private fun inferenceRecord(){
        status = INFERENCE
        if (recordingUriString != null){
            val recordingUri = Uri.parse(recordingUriString)
            viewModel.getClassificationResult(recordingUri)
        }

        uiChange(status)
    }

    // 准备下次录音
    private fun prepareRecord(){
        status = START
        uiChange(status)
    }

    // 改变UI
    private fun uiChange(status: Int){
        when (status){
            START -> {
                // 计时器向下移动
                val animator = ObjectAnimator.ofFloat(binding.durationDisplay,
                    "translationY", 0f).apply {
                    duration = 1000
                    start()
                }
                // 隐藏波形，识别结果，重置录音时间，改变按钮形式
                binding.audioRecordView.visibility = View.INVISIBLE
                binding.durationDisplay.text = "00:00.00"
                binding.recordBtn.setImageResource(R.drawable.ic_microphone_vector)
                binding.resultDisplay.visibility = View.INVISIBLE
            }

            RECORDING -> {
                // 计时器向上移动
                val animator = ObjectAnimator.ofFloat(binding.durationDisplay,
                    "translationY", -300f).apply {
                    duration = 1000
                    start()
                }
                // 显示录音波形画面
                binding.audioRecordView.visibility = View.VISIBLE
                binding.audioRecordView.recreate()
                // 改变按钮形式
                binding.recordBtn.setImageResource(R.drawable.ic_stop_vector)
            }

            STOP -> {
                // 改变按钮形式
                binding.recordBtn.setImageResource(R.drawable.yes)
                binding.recordBtn.setBackgroundResource(R.drawable.green_circle_background)
                binding.cancelBtn.visibility = View.VISIBLE
            }

            INFERENCE -> {
                // 显示识别结果
                binding.resultDisplay.visibility = View.VISIBLE
                viewModel.audioClassLiveData.observe(viewLifecycleOwner, Observer { result ->
                    val audioClass = result.getOrNull()
                    audioClassDisplay = audioClass ?: "网络有问题，无法得到结果"
                    binding.resultDisplay.text = audioClassDisplay
                })
                // 改变按钮形式
                binding.cancelBtn.visibility = View.INVISIBLE
                binding.recordBtn.setImageResource(R.drawable.next)
                binding.recordBtn.setBackgroundResource(R.drawable.circle_background)
            }
        }
    }

    // 显示录音时长
    private fun updateDuration(duration: Int){
        var minutes = duration / 1000 / 60
        var seconds = duration / 1000 % 60
        var milliseconds = duration % 1000 / 10 // 只显示2位毫秒
        var displayTime = formatTime(minutes) + ":" + formatTime(seconds) + "." + formatTime(milliseconds)
        binding.durationDisplay.text = displayTime
    }

    // 显示时间，如果只有个位，在前面补0
    private fun formatTime(time: Int): String{
        var timeDisplay = if (time < 10) {
            "0$time"
        } else{
            "$time"
        }
        return timeDisplay
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.UpdatemaxAmplitude -> binding.audioRecordView.update(event.getInt())
            MessageType.UpdateDuration -> updateDuration(event.getInt())
            // MessageType.RecordUri -> recordingPath = event.getString()
            MessageType.RecordUri -> recordingUriString = event.getString()
        }
    }

    // 删除录音
    private fun deleteRecording(){
        if (recordingUriString != null){
            val recordingUri = Uri.parse(recordingUriString)
            val file = File(recordingUri.path)
            file.delete(MyApplication.context)
        }
    }

    private fun File.delete(context: Context): Boolean {
        var selectionArgs = arrayOf(this.absolutePath)
        val contentResolver = context.contentResolver
        var where: String? = null
        var filesUri: Uri? = null
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            filesUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            where = MediaStore.Audio.Media._ID + "=?"
            selectionArgs = arrayOf(this.name)
        } else {
            where = MediaStore.MediaColumns.DATA + "=?"
            filesUri = MediaStore.Files.getContentUri("external")
        }
        val int = contentResolver.delete(filesUri!!, where, selectionArgs)
        return !this.exists()
    }

}

