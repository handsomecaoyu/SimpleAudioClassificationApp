package com.example.sound.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.databinding.FragmentHistoryBinding
import com.example.sound.helps.DATE_ADDED
import com.example.sound.helps.HISTORY_LOG_TAG
import com.example.sound.helps.HOME_LOG_TAG
import com.example.sound.logic.database.DatabaseManager
import com.example.sound.logic.model.Audio
import com.example.sound.ui.audio.AudioAdapter
import com.example.sound.ui.audio.AudioViewModel
import kotlinx.coroutines.*


class HistoryFragment : Fragment() {
    private lateinit var adapter: AudioAdapter
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy { ViewModelProvider(this).get(AudioViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager
        if (hasPermissions(activity as Context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            // 获得包含音频信息的列表
            var audioList = viewModel.getAudioInfo()
            audioList = addClassResult(audioList)
            val audioListWithDate = addDateToList(audioList)
            adapter = AudioAdapter(this, audioListWithDate)
            binding.recyclerView.adapter = adapter

            // 下拉刷新设置
            binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(MyApplication.context.getColor(R.color.colorPrimary))
            binding.swipeRefreshLayout.setColorSchemeColors(MyApplication.context.getColor(R.color.white))
            binding.swipeRefreshLayout.setOnRefreshListener {
                var audioList = viewModel.getAudioInfo()
                audioList = addClassResult(audioList)
                val audioListWithDate = addDateToList(audioList)
                adapter = AudioAdapter(this, audioListWithDate)
                binding.recyclerView.adapter = adapter
                binding.swipeRefreshLayout.isRefreshing = false
            }
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
}