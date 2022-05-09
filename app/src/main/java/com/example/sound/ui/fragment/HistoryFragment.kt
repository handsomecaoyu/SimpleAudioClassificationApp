package com.example.sound.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sound.databinding.FragmentHistoryBinding

import com.example.sound.ui.audio.AudioAdapter
import com.example.sound.ui.audio.AudioViewModel


class HistoryFragment : Fragment() {
    private lateinit var adapter: AudioAdapter
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    val viewModel by lazy { ViewModelProvider(this).get(AudioViewModel::class.java) }

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

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isgranted ->
            if (isgranted) {
                val audioList = viewModel.getAudioName()
                adapter = AudioAdapter(this, audioList)
                binding.recyclerView.adapter = adapter
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager
        if (hasPermissions(activity as Context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            val audioList = viewModel.getAudioName()
            adapter = AudioAdapter(this, audioList)
            binding.recyclerView.adapter = adapter
        } else {
            permReqLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

}