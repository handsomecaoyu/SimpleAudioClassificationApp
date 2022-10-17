package com.example.sound.ui.audio

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
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

class AudioAdapter(private val fragment: HistoryFragment, private var audioList: MutableList<Audio>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // 是否被选中的集合
    var multiSelectedSet = mutableSetOf<Int>()
    var isMultiSelecting = false
    private val cancelMultiSelectionLiveData = MutableLiveData<Boolean>()

    // 用于显示音频信息
    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val audioTime: TextView = view.findViewById(R.id.audioTime)  // 音频录制的时间，时分秒
        private val audioDuration: TextView = view.findViewById(R.id.audioDuration)  // 时长
        private val audioDate: TextView = view.findViewById(R.id.audioDate)  // 音频录制的日期
        private val audioClass: TextView = view.findViewById(R.id.audioClass)  // 结果类型
        val audioIcon: ImageView = view.findViewById(R.id.audioIcon)
        @SuppressLint("UseCompatLoadingForDrawables", "SimpleDateFormat")
        fun bind(audio: Audio, position: Int) {
            // 在卡片中显示各种信息
            audioTime.text = audio.dateAddedString.split('_')[1]
            audioDuration.text = audio.duration
            audioDate.text = SimpleDateFormat("YYYY/M/d")
                .format(audio.dateAddedTimeStamp*1000)
            // 根据不同的情况等级显示不同的颜色
            when (audio.classResponse.level) {
                NORMAL -> audioClass.background =
                    MyApplication.context.getDrawable(R.drawable.green_horizontal_line)
                WARNING -> audioClass.background =
                    MyApplication.context.getDrawable(R.drawable.orange_horizontal_line)
                ABNORMAL -> audioClass.background =
                    MyApplication.context.getDrawable(R.drawable.red_horizontal_line)
            }
            audioClass.text = audio.classResponse.result
            // 根据选择状态设置图片
            // 这段看似比较多余，因为在setOnClickListener中已经有了改变图片的部分，
            // 但是如果没有这段，就会出现点击之后，相隔十多个的选项也会变成选中的图片，很离谱的bug，我也不懂为什么
            // 但这段确实起作用的，
            if (audio.isSelected)
                audioIcon.setImageResource(R.drawable.selected)
            else
                audioIcon.setImageResource(R.drawable.play)
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
                val holderTemp = AudioViewHolder(view)

                // 长按进入多选模式
                holderTemp.itemView.setOnLongClickListener {
                    isMultiSelecting = true
                    cancelMultiSelectionLiveData.value = false
                    EventBus.getDefault()
                        .post(MessageEvent(MessageType.AudioItemLongPressed).put(isMultiSelecting))
                    multiSelectedSet.add(holderTemp.layoutPosition)
                    audioList[holderTemp.layoutPosition].isSelected = true
                    holderTemp.audioIcon.setImageResource(R.drawable.selected)
                    true
                }

                // 设置单选
                holderTemp.itemView.setOnClickListener {
                    // 在多选模式下
                    if (isMultiSelecting){
                        // 如果已经选中，取消选中
                        if (multiSelectedSet.contains(holderTemp.layoutPosition)) {
                            multiSelectedSet.remove(holderTemp.layoutPosition)
                            audioList[holderTemp.layoutPosition].isSelected = false
                            holderTemp.audioIcon.setImageResource(R.drawable.play)
                        }
                        // 还未选中，则选中
                        else {
                            multiSelectedSet.add(holderTemp.layoutPosition)
                            audioList[holderTemp.layoutPosition].isSelected = true
                            holderTemp.audioIcon.setImageResource(R.drawable.selected)
                        }
                    }
                }

                // 观察是否取消多选
                cancelMultiSelectionLiveData.observe(holderTemp.itemView.context as LifecycleOwner) {
                    if (it) {
                        multiSelectedSet.remove(holderTemp.layoutPosition)
                        holderTemp.audioIcon.setImageResource(R.drawable.play)
                    }
                }

                holder = holderTemp
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

    // 取消多选
    fun cancelMultiSelection(){
        isMultiSelecting = false
        cancelMultiSelectionLiveData.value = true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newAudioList: MutableList<Audio>){
        audioList = newAudioList
        this.notifyDataSetChanged()
    }
}