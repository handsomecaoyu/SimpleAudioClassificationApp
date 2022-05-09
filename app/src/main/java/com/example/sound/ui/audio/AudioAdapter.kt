package com.example.sound.ui.audio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sound.R
import com.example.sound.logic.audio.Audio
import com.example.sound.ui.fragment.HistoryFragment

class AudioAdapter(private val fragment: HistoryFragment, private val audioList: List<Audio>) :
    RecyclerView.Adapter<AudioAdapter.ViewHolder>(){
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val audioName: TextView = view.findViewById(R.id.audioName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audio_item,
            parent, false)
        val holder = ViewHolder(view)
        holder.audioName.setOnClickListener{
            val position = holder.adapterPosition
            val audio = audioList[position]
            Toast.makeText(parent.context, audio.title, Toast.LENGTH_LONG).show()
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audio =audioList[position]
        holder.audioName.text = audio.title
    }

    override fun getItemCount(): Int {
        return audioList.size
    }
}