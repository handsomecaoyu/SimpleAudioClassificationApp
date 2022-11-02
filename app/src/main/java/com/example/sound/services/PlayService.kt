package com.example.sound.services

import android.app.Service
import android.app.usage.UsageEvents.Event
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import com.example.sound.MyApplication
import com.example.sound.logic.MessageEvent
import com.example.sound.logic.MessageType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PlayService: Service(), MediaPlayer.OnPreparedListener {

    private var mMediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        // 注册EventBus，这是一个事件总线，用于不同组件之间方便通信
        EventBus.getDefault().register(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val uriString = intent.getStringExtra("uriString")
        val uri = Uri.parse(uriString)
        startPlay(uri)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
        println("play")
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        stopPlay()
        super.onDestroy()
    }

    private fun startPlay(uri: Uri){
        mMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            )
            setDataSource(MyApplication.context, uri)
            setOnCompletionListener {
                EventBus.getDefault().post(MessageEvent(MessageType.Finish).put(true))
                stopPlay()
            }
            // 异步准备
            setOnPreparedListener(this@PlayService)
            prepareAsync() // prepare async to not block main thread
        }
    }

    private fun pausePlay() {
        println("pause")
        mMediaPlayer?.pause()
    }

    private fun resumePlay() {
        println("resume")
        mMediaPlayer?.start()
    }

    private fun stopPlay(){
        println("stop")
        mMediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mMediaPlayer = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.Pause -> pausePlay()
            MessageType.Resume -> resumePlay()
        }
    }


}