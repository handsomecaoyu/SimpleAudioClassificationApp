package com.example.sound.ui.audio

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.helps.*
import com.example.sound.logic.database.DatabaseManager
import com.example.sound.logic.model.Audio
import com.example.sound.ui.fragment.HistoryFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class AudioAdapter(private val fragment: HistoryFragment, private val audioList: MutableList<Audio>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // 用于显示音频信息
    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val audioTime: TextView = view.findViewById(R.id.audioTime)
        private val audioDuration: TextView = view.findViewById(R.id.audioDuration)
        private val audioDate: TextView = view.findViewById(R.id.audioDate)
        private val audioClass: TextView = view.findViewById(R.id.audioClass)
        fun bind(audio: Audio) {
            audioTime.text = audio.dateAddedString.split('_')[1]
            audioDuration.text = audio.duration
            audioDate.text = SimpleDateFormat("YYYY/M/d").format(audio.dateAddedTimeStamp*1000)
            when (audio.classResponse.level) {
                NORMAL -> audioClass.background = MyApplication.context.getDrawable(R.drawable.green_textview)
                WARNING -> audioClass.background = MyApplication.context.getDrawable(R.drawable.orange_textview)
                ABNORMAL -> audioClass.background = MyApplication.context.getDrawable(R.drawable.red_textview)
            }
            audioClass.text = audio.classResponse.result
        }
    }

    // 用于显示日期信息
    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val dateAdded: TextView = view.findViewById(R.id.dateAdded)
        private val divider: View = view.findViewById(R.id.dateDivider)
        fun bind(audio: Audio, position: Int) {
            dateAdded.text = audio.dateAddedString
            // 隐藏第一个的分割线
            if (this.layoutPosition == 0) {
                divider.visibility = View.INVISIBLE
            } else
                divider.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        lateinit var view: View
        lateinit var holder: RecyclerView.ViewHolder
        // 根据不同的viewType选择holder
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

    // 绑定
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val audio = audioList[position]
        when (audio.itemType) {
            AUDIO -> (holder as AudioViewHolder).bind(audio)
            DATE_ADDED -> (holder as DateViewHolder).bind(audio, position)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    // 获得item的类型，跟onCreateViewHolder中的when (viewType)对应
    override fun getItemViewType(position: Int): Int {
        return audioList[position].itemType
    }
}