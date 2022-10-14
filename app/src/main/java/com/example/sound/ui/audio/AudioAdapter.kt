package com.example.sound.ui.audio

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.sound.MyApplication
import com.example.sound.R
import com.example.sound.helps.*
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import com.example.sound.logic.model.Audio
import com.example.sound.ui.fragment.HistoryFragment
import org.greenrobot.eventbus.EventBus

class AudioAdapter(private val fragment: HistoryFragment, private val audioList: MutableList<Audio>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // 是否被选中的集合
    var multiSelectedSet = mutableSetOf<Int>()
    var isMultiSelecting = false
    val cancelMultiSelectionLiveData = MutableLiveData<Boolean>()

    // 用于显示音频信息
    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val audioTime: TextView = view.findViewById(R.id.audioTime)  // 音频录制的时间，时分秒
        private val audioDuration: TextView = view.findViewById(R.id.audioDuration)  // 时长
        private val audioDate: TextView = view.findViewById(R.id.audioDate)  // 音频录制的日期
        private val audioClass: TextView = view.findViewById(R.id.audioClass)  // 结果类型
        private val audioIcon: ImageView = view.findViewById(R.id.audioIcon)
        private val viewLifecycleOwner = view.context as LifecycleOwner
        @SuppressLint("UseCompatLoadingForDrawables", "SimpleDateFormat")
        fun bind(audio: Audio, position: Int) {
            // 在卡片中显示各种信息
            audioTime.text = audio.dateAddedString.split('_')[1]
            audioDuration.text = audio.duration
            audioDate.text = SimpleDateFormat("YYYY/M/d").format(audio.dateAddedTimeStamp*1000)
            // 根据不同的情况等级显示不同的颜色
            when (audio.classResponse.level) {
                NORMAL -> audioClass.background = MyApplication.context.getDrawable(R.drawable.green_textview)
                WARNING -> audioClass.background = MyApplication.context.getDrawable(R.drawable.orange_textview)
                ABNORMAL -> audioClass.background = MyApplication.context.getDrawable(R.drawable.red_textview)
            }
            audioClass.text = audio.classResponse.result

            // 长按进入多选模式
            this.itemView.setOnLongClickListener{
                isMultiSelecting = true
                cancelMultiSelectionLiveData.value = false
                EventBus.getDefault().post(MessageEvent(MessageType.AudioItemLongPressed).put(isMultiSelecting))
                multiSelectedSet.add(position)
                audioIcon.setImageResource(R.drawable.selected)
                true
            }

            // 设置单选
            this.itemView.setOnClickListener {
                // 在多选模式下
                if (isMultiSelecting){
                    // 如果已经选中，取消选中
                    if (multiSelectedSet.contains(position)) {
                        multiSelectedSet.remove(position)
                        audioIcon.setImageResource(R.drawable.play)
                    }
                    // 还未选中，则选中
                    else {
                        multiSelectedSet.add(position)
                        audioIcon.setImageResource(R.drawable.selected)
                    }
                }
            }

            cancelMultiSelectionLiveData.observe(viewLifecycleOwner, Observer {
                if (it){
                    multiSelectedSet.remove(position)
                    audioIcon.setImageResource(R.drawable.play)
                }
            })

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
            AUDIO -> (holder as AudioViewHolder).bind(audio, position)
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

    fun cancelMultiSelection(){
        isMultiSelecting = false
        cancelMultiSelectionLiveData.value = true
    }
}