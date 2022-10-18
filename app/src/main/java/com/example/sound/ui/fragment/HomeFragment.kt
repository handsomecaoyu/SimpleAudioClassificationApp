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
import android.util.Log
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
import com.example.sound.helps.*
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import com.example.sound.logic.database.DatabaseManager
import com.example.sound.logic.model.ClassEntity
import com.example.sound.logic.model.ClassResponse
import com.example.sound.services.RecordService
import com.example.sound.ui.audio.AudioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    // 协程相关
    private val homeJob = Job()
    private val homeScope = CoroutineScope(homeJob)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 注册EventBus，这是一个事件总线，用于不同组件之间方便通信
        EventBus.getDefault().register(this)

        // 一开始隐藏取消按钮、声音波形和识别结果
        binding.audioRecordView.visibility = View.INVISIBLE
        binding.cancelBtn.visibility = View.INVISIBLE
        binding.resultDisplay.visibility = View.INVISIBLE

        // 设置录音按键动作
        binding.recordBtn.setOnClickListener{
            if (hasPermissions(activity as Context,
                    arrayOf(Manifest.permission.RECORD_AUDIO,
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

        // 注册音频类别的观察对象
        // 注意这部分不能写在重复调用的地方，不然会多次注册observer
        viewModel.audioClassLiveData.observe(viewLifecycleOwner, Observer { result ->
            val audioClass = result.getOrNull()
            if (audioClass != null){
                audioClassDisplay = audioClass.result
                // 将结果添加到数据库中
                recordingUriString?.let { insertClass(it, audioClass) }
            } else {
                val emptyClassResponse = ClassResponse(result="网络异常")
                audioClassDisplay = emptyClassResponse.result
                recordingUriString?.let { insertClass(it, emptyClassResponse) }
            }
            binding.resultDisplay.text = audioClassDisplay
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        _binding = null
        homeJob.cancel()
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
        EventBus.getDefault().post(MessageEvent(MessageType.NewAudioAdded).put(true))
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
                binding.recordBtn.setImageResource(R.drawable.microphone)
                binding.resultDisplay.visibility = View.INVISIBLE
                binding.cancelBtn.visibility = View.INVISIBLE
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
                binding.recordBtn.setImageResource(R.drawable.stop)
            }

            STOP -> {
                // 改变按钮形式
                binding.recordBtn.setImageResource(R.drawable.selected_white)
                binding.cancelBtn.visibility = View.VISIBLE
            }

            INFERENCE -> {
                // 显示识别结果
                binding.resultDisplay.visibility = View.VISIBLE
                // 改变按钮形式
                binding.cancelBtn.visibility = View.INVISIBLE
                binding.recordBtn.setImageResource(R.drawable.next)
            }
        }
    }

    // 显示录音时长
    private fun updateDuration(duration: Int){
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        val milliseconds = duration % 1000 / 10 // 只显示2位毫秒
        val displayTime = formatTime(minutes) + ":" + formatTime(seconds) + "." + formatTime(milliseconds)
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


    // EventBus的消息队列
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
            MyApplication.context.contentResolver.delete(recordingUri, null, null);
        }
    }

    // 向数据库中插入音频类别的信息
    private fun insertClass(uriString: String, classResponse: ClassResponse){
        homeScope.launch {
            // 最后一个是在Media.Audio中的主键id
            val mediaId = uriString.split(File.separator).last().toLong()
            val audioClassEntity = ClassEntity(mediaId, classResponse)
            try {
                DatabaseManager.db.classDao.insert(audioClassEntity)
            } catch (exception: Exception) {
                Log.e(HOME_LOG_TAG, exception.message.toString())
            }
        }
    }

}

