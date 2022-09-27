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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.databinding.FragmentHomeBinding
import com.example.sound.helps.PAUSE
import com.example.sound.helps.RECORDING
import com.example.sound.helps.START
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import com.example.sound.logic.audio.RecordService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var status = START
    private var recordingPath : String? = null

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

    // 获取录音权限
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isgranted ->
            if (isgranted) {
                startRecord()
            }
        }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        // 一开始隐藏取消按钮和声音波形
        binding.audioRecordView.visibility = View.INVISIBLE
        binding.cancelBtn.visibility = View.INVISIBLE

        // 设置录音按键动作
        binding.recordBtn.setOnClickListener{
            if (hasPermissions(activity as Context, arrayOf(Manifest.permission.RECORD_AUDIO))) {
                when (status) {
                    START -> startRecord()
                    RECORDING -> stopRecord()
                    else -> prepareRecord()
                }
            } else {
                permReqLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        // 设置取消按键动作
        binding.cancelBtn.setOnClickListener {
            deleteRecording()
            prepareRecord()
        }
    }

    // 判断有无权限
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // 开始录音
    private fun startRecord(){
        // 计时器向上移动
        val animator = ObjectAnimator.ofFloat(binding.durationDisplay,
            "translationY", -300f).apply {
            duration = 1000
            start()
        }
        val intent = Intent(MyApplication.context, RecordService::class.java)
        binding.audioRecordView.visibility = View.VISIBLE
        MyApplication.context.startService(intent)
        binding.audioRecordView.recreate()
        status = RECORDING
        buttonChange(status)
    }

    // 停止录音
    private fun stopRecord(){
        val intent = Intent(MyApplication.context, RecordService::class.java)
        MyApplication.context.stopService(intent)
        status = PAUSE
        buttonChange(status)
    }

    // 准备下次录音
    private fun prepareRecord(){
        // 计时器向下移动
        val animator = ObjectAnimator.ofFloat(binding.durationDisplay,
            "translationY", 0f).apply {
            duration = 1000
            start()
        }
        binding.audioRecordView.visibility = View.INVISIBLE
        status = START
        binding.durationDisplay.text = "00:00.00"
        buttonChange(status)
    }

    private fun buttonChange(status: Int){
        when (status){
            START -> {
                binding.recordBtn.setImageResource(R.drawable.ic_microphone_vector)
                binding.recordBtn.setBackgroundResource(R.drawable.circle_background)
                binding.cancelBtn.visibility = View.INVISIBLE
            }
            RECORDING -> {
                binding.recordBtn.setImageResource(R.drawable.ic_stop_vector)
            }
            PAUSE -> {
                binding.recordBtn.setImageResource(R.drawable.yes)
                binding.recordBtn.setBackgroundResource(R.drawable.green_circle_background)
                binding.cancelBtn.visibility = View.VISIBLE
            }
        }
    }

    // 显示录音时长
    private fun updateDuration(duration: Int){
        var minutes = duration / 1000 / 60
        var seconds = duration / 1000 % 60
        var milliseconds = duration % 1000 / 10 // 只显示2位毫秒
        var displatTime = formatTime(minutes) + ":" + formatTime(seconds) + "." + formatTime(milliseconds)
        binding.durationDisplay.text = displatTime
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
            MessageType.RecordUri -> recordingPath = event.getString()
        }
    }

    // 删除录音
    private fun deleteRecording(){
        if (recordingPath != null){
            val file = File(recordingPath)
            file.delete(MyApplication.context)
        }
    }



}

fun File.delete(context: Context): Boolean {
    var selectionArgs = arrayOf(this.absolutePath)
    val contentResolver = context.getContentResolver()
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