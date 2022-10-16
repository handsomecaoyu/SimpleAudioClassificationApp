package com.example.sound.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sound.ActivityCollector
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.databinding.FragmentHistoryBinding
import com.example.sound.helps.DATE_ADDED
import com.example.sound.helps.HISTORY_LOG_TAG
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import com.example.sound.logic.database.DatabaseManager
import com.example.sound.logic.model.Audio
import com.example.sound.ui.audio.AudioAdapter
import com.example.sound.ui.audio.AudioViewModel
import com.example.sound.utils.delete
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


class HistoryFragment : Fragment() {
    private lateinit var adapter: AudioAdapter
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy { ViewModelProvider(this).get(AudioViewModel::class.java) }
    private lateinit var audioListWithDate: MutableList<Audio>
    private var isMultiSelecting = false

    // 协程相关
    private val historyJob = Job()
    private val historyScope = CoroutineScope(historyJob)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        historyJob.cancel()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 注册EventBus，这是一个事件总线，用于不同组件之间方便通信
        EventBus.getDefault().register(this)

        // 绑定视图
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager

        if (hasPermissions(activity as Context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            // 获得包含音频信息的列表
            var audioList = viewModel.getAudioInfo()
            audioList = addClassResult(audioList)
            audioListWithDate = addDateToList(audioList)
            adapter = AudioAdapter(this, audioListWithDate)
            binding.recyclerView.adapter = adapter

            // 隐藏多选时的工作栏
            binding.multiSelectedMenu.visibility = View.INVISIBLE

            // 取消多选
            binding.selectedCancel.setOnClickListener {
                isMultiSelecting = false
                multiSelectedMenuChange(isMultiSelecting)
            }

            // 删除
            binding.selectedDelete.setOnClickListener {
                deleteAudios()
                isMultiSelecting = false
                multiSelectedMenuChange(isMultiSelecting)
            }

            // 下拉刷新设置
            binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(MyApplication.context.getColor(R.color.colorPrimary))
            binding.swipeRefreshLayout.setColorSchemeColors(MyApplication.context.getColor(R.color.white))
            binding.swipeRefreshLayout.setOnRefreshListener {
                var audioList = viewModel.getAudioInfo()
                audioList = addClassResult(audioList)
                audioListWithDate = addDateToList(audioList)
                adapter = AudioAdapter(this, audioListWithDate)
                binding.recyclerView.adapter = adapter
                binding.swipeRefreshLayout.isRefreshing = false
            }

            // 设置返回按钮的监听
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isMultiSelecting){
                            isMultiSelecting = false
                            multiSelectedMenuChange(isMultiSelecting)
                        } else{
                            ActivityCollector.finishAll()
                        }
                    }
                })


        } else {
            permReqLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) // 读取权限
        }

    }

    // 检测有无权限
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // 获得权限
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                val audioList = viewModel.getAudioInfo()
                adapter = AudioAdapter(this, audioList)
                binding.recyclerView.adapter = adapter
            }
        }

    // 向音频列表中添加音频信息
    private fun addDateToList(audioList: List<Audio>): MutableList<Audio>{
        var audioListWithDate: MutableList<Audio> = ArrayList()
        var dateAddedTemp = ""
        var dateAdded = ""
        for (audio in audioList){
            dateAddedTemp = audio.dateAddedString.split("_")[0]
            if (dateAdded != dateAddedTemp) {
                dateAdded = dateAddedTemp
                audioListWithDate.add(Audio(0, "", "", 0, dateAdded,"", 0, DATE_ADDED))
            }
            audioListWithDate.add(audio)
        }
        return audioListWithDate
    }

    // 向音频列表添加分类结果
    private fun addClassResult(audioList: ArrayList<Audio>): ArrayList<Audio>{
        for (audio in audioList)
            audio.classResponse = viewModel.getAudioClassFromDB(audio.id)
        return audioList
    }

    // EventBus的消息队列
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.AudioItemLongPressed -> {
                isMultiSelecting = true
                multiSelectedMenuChange(isMultiSelecting)
            }
        }
    }

    // 根据是否进入多选状态显示工作栏
    private fun multiSelectedMenuChange(isMultiSelectingTemp: Boolean){
        if (isMultiSelectingTemp) {
            binding.multiSelectedMenu.visibility = View.VISIBLE
        } else {
            binding.multiSelectedMenu.visibility = View.INVISIBLE
            adapter.cancelMultiSelection()
        }
    }

    // 删除对应的文件
    private fun deleteAudios(){
        for (position in adapter.multiSelectedSet) {

            val audioTemp = audioListWithDate[position]
            historyScope.launch{
                withContext(Dispatchers.IO){
                    try {
                        // 从room数据库中删除记录
                        DatabaseManager.db.classDao.deleteById(audioTemp.id)
                        // 删除文件
                        println(audioTemp.uriString)
                        val uri = Uri.parse(audioTemp.uriString)
//                        val file = File(audioTemp.path)
//                        file.delete(MyApplication.context)
                        MyApplication.context.contentResolver.delete(uri, null, null);

                    } catch (e: Exception) {
                        Log.e(HISTORY_LOG_TAG, e.toString())
                    }
                    Log.v(HISTORY_LOG_TAG,"delete " + audioTemp.uriString + " in position " + position.toString())
                }
            }
        }
    }
}