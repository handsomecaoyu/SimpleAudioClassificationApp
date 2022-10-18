package com.example.sound.services

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import com.example.sound.MyApplication

private const val ACTION_PLAY: String = "com.example.action.PLAY"

class PlayService: Service(), MediaPlayer.OnPreparedListener {

    private var mMediaPlayer: MediaPlayer? = null

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
    }

    override fun onDestroy() {
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
            // 异步准备
            setOnPreparedListener(this@PlayService)
            prepareAsync() // prepare async to not block main thread
        }
    }

    private fun stopPlay(){
        mMediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mMediaPlayer = null
    }


}