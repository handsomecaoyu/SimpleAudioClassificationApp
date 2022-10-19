package com.example.sound.ui.audio

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.example.sound.services.PlayService
import com.example.sound.ui.fragment.HistoryFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AudioAdapter(private val fragment: HistoryFragment, private var audioList: MutableList<Audio>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // 是否被选中的集合
    var multiSelectedSet = mutableSetOf<Int>()
    // 是否进入多选状态的标志
    private var isMultiSelecting = false
    // 上一个展开的位置
    private var lastExpendedPosition = -1
    // 上一个播放音频的位置
    private var lastPlayAudioPosition = -1


    // 用于显示音频信息
    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val audioTime: TextView = view.findViewById(R.id.audioTime)  // 音频录制的时间，时分秒
        private val audioDuration: TextView = view.findViewById(R.id.audioDuration)  // 时长
        private val audioDate: TextView = view.findViewById(R.id.audioDate)  // 音频录制的日期
        private val audioClass: TextView = view.findViewById(R.id.audioClass)  // 结果类型
        val audioIcon: ImageView = view.findViewById(R.id.audioIcon)
        val subItem: ConstraintLayout = view.findViewById(R.id.sub)
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
            if (audio.isSelected) {
                audioIcon.setImageResource(R.drawable.selected)
            } else
                when (audio.status){
                    FINISHED -> audioIcon.setImageResource(R.drawable.play)
                    PAUSED -> audioIcon.setImageResource(R.drawable.play)
                    PLAYING -> audioIcon.setImageResource(R.drawable.pause)
                }

            // 决定是否展开子项
            if (audio.isExpended) {
                subItem.visibility = View.VISIBLE
            } else {
                subItem.visibility = View.GONE
            }
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

                // 关于按键点击的监听最好写在这里，如果写在onBindViewHolder，每次刷到它都会重新bind一遍
                // 长按进入多选模式
                holderTemp.itemView.setOnLongClickListener {
                    val position = holderTemp.layoutPosition
                    val audio = audioList[position]
                    isMultiSelecting = true
                    EventBus.getDefault()
                        .post(MessageEvent(MessageType.AudioItemLongPressed).put(isMultiSelecting))
                    multiSelectedSet.add(position)
                    audio.isSelected = true
                    holderTemp.audioIcon.setImageResource(R.drawable.selected)
                    true
                }

                // 设置整个卡片的点击事件
                holderTemp.itemView.setOnClickListener {
                    val position = holderTemp.layoutPosition
                    val audio = audioList[position]
                    // 在多选模式下
                    if (isMultiSelecting){
                        // 如果已经选中，取消选中
                        if (audio.isSelected) {
                            multiSelectedSet.remove(position)
                            audio.isSelected = false
                            holderTemp.audioIcon.setImageResource(R.drawable.play)
                        }
                        // 还未选中，则选中
                        else {
                            multiSelectedSet.add(position)
                            audio.isSelected = true
                            holderTemp.audioIcon.setImageResource(R.drawable.selected)
                        }
                    }
                    else {
                        audio.isExpended = !audio.isExpended
                        if (lastExpendedPosition >= 0 && lastExpendedPosition!=holderTemp.layoutPosition)
                            audioList[lastExpendedPosition].isExpended = false
                        // 收起之前的下拉
                        if (lastExpendedPosition >= 0)
                            notifyItemChanged(lastExpendedPosition)
                        // 将点中的下拉
                        notifyItemChanged(holderTemp.layoutPosition)
                        lastExpendedPosition = holderTemp.layoutPosition

                    }
                }

                // 设置播放图标的点击事件
                holderTemp.audioIcon.setOnClickListener{
                    val position = holderTemp.layoutPosition
                    val audio = audioList[position]
                    // 设置下拉事件
                    // 点击播放的时候也会下拉
                    if (!audio.isExpended) {
                        audio.isExpended = true
                        if (lastExpendedPosition >= 0 && lastExpendedPosition != position)
                            audioList[lastExpendedPosition].isExpended = false
                        // 收起之前的下拉
                        if (lastExpendedPosition >= 0)
                            notifyItemChanged(lastExpendedPosition)
                        // 将点中的下拉
                        notifyItemChanged(holderTemp.layoutPosition)
                        lastExpendedPosition = holderTemp.layoutPosition
                    }

                    // 设置播放事件
                    when (audio.status){
                        // 该音频处于停止状态，点击播放音频
                        FINISHED -> {
                            // 关闭之前播放的音频
                            if (lastPlayAudioPosition >= 0 && lastPlayAudioPosition != position) {
                                audioList[lastPlayAudioPosition].status = FINISHED
                                stopAudio()
                                notifyItemChanged(lastPlayAudioPosition)
                            }
                            audio.status = PLAYING
                            holderTemp.audioIcon.setImageResource(R.drawable.pause)
                            playAudio(position)
                            lastPlayAudioPosition = position

                        }
                        // 该音频正在处于播放状态，点击暂停音频播放
                        PLAYING -> {
                            audio.status = PAUSED
                            pauseAudio()
                            holderTemp.audioIcon.setImageResource(R.drawable.play)
                        }

                        PAUSED -> {
                            audio.status = PLAYING
                            resumeAudio()
                            holderTemp.audioIcon.setImageResource(R.drawable.pause)
                        }
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

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        EventBus.getDefault().register(this)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromRecyclerView(recyclerView)
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
        for (position in multiSelectedSet){
            audioList[position].isSelected =false
            notifyItemChanged(position)
        }
        multiSelectedSet.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newAudioList: MutableList<Audio>){
        audioList = newAudioList
        this.notifyDataSetChanged()
    }

    // 开始播放音乐
    private fun playAudio(position: Int){
        val intent = Intent(MyApplication.context, PlayService::class.java)
        intent.putExtra("uriString", audioList[position].uriString)
        MyApplication.context.startService(intent)
    }

    // 暂停播放
    private fun pauseAudio() {
        EventBus.getDefault().post(MessageEvent(MessageType.Pause).put(true))
    }

    // 继续播放
    private fun resumeAudio() {
        EventBus.getDefault().post(MessageEvent(MessageType.Resume).put(true))
    }

    // 结束播放
    private fun stopAudio() {
        val intent = Intent(MyApplication.context, PlayService::class.java)
        MyApplication.context.stopService(intent)
    }

    // 完成播放
    private fun finishPlay() {
        audioList[lastPlayAudioPosition].status = FINISHED
        notifyItemChanged(lastPlayAudioPosition)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.Finish -> finishPlay()

        }
    }
}