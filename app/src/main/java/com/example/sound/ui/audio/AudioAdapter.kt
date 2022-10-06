package com.example.sound.ui.audio

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sound.AudioInfo
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.helps.AUDIO
import com.example.sound.helps.DATE_ADDED
import com.example.sound.logic.model.Audio
import com.example.sound.ui.fragment.HistoryFragment

class AudioAdapter(private val fragment: HistoryFragment, private val audioList: MutableList<Audio>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var audioTime: TextView = view.findViewById(R.id.audioTime)
        fun bind(audio: Audio) {
            audioTime.text = audio.dateAddedString.split('_')[1]
        }
    }

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val dateAdded: TextView = view.findViewById(R.id.dateAdded)
        fun bind(audio: Audio) {
            dateAdded.text = audio.dateAddedString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        lateinit var view: View
        lateinit var holder: RecyclerView.ViewHolder
        when (viewType) {
            AUDIO -> {
                view = inflater.inflate(R.layout.audio_item, parent, false)
                holder = AudioViewHolder(view)

//                holder.audioName.setOnClickListener{
//                    val position = holder.adapterPosition
//                    val audio = audioList[position]
//                    val intent = Intent(MyApplication.context, AudioInfo::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    MyApplication.context.startActivity(intent)
//                }
            }
            DATE_ADDED -> {
                view = inflater.inflate(R.layout.date_added, parent, false)
                holder = DateViewHolder(view)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val audio = audioList[position]
        when (audio.itemType) {
            AUDIO -> (holder as AudioViewHolder).bind(audio)
            DATE_ADDED -> (holder as DateViewHolder).bind(audio)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun getItemViewType(position: Int): Int {
        return audioList[position].itemType
    }
}