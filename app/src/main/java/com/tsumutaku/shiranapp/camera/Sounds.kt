package com.tsumutaku.shiranapp.camera

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.tsumutaku.shiranapp.R


class Sounds constructor(context: Context) {

    private var soundPool: SoundPool? = null


    companion object {

        var SOUND_DRUMROLL = 0
        var SAD_TROMBONE = 10

        var INSTANCE:Sounds? = null
        fun getInstance(context: Context) =
            INSTANCE ?: Sounds(context).also {
                INSTANCE = it
            }

    }

    init {
        createSoundPool()
        loadSoundIDs(context)
    }

    private fun createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool()
        } else {
            createOldSoundPool()
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createNewSoundPool() {
        val attributes = AudioAttributes.Builder().apply {
            setUsage(AudioAttributes.USAGE_GAME)
            setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)

        }.build()

        soundPool = SoundPool.Builder().apply {
            setMaxStreams(2)
            setAudioAttributes(attributes)
        }.build()
    }


    private fun createOldSoundPool() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
    }

    private fun loadSoundIDs(context:Context) {
        soundPool?.let {
            println("サウンドファイルロード")
            SOUND_DRUMROLL = it.load(context, R.raw.decision8, 1)
            SAD_TROMBONE = it.load(context, R.raw.cheers, 1)
        }
    }


    fun playSound(soundID:Int) {
        soundPool?.let{
            it.play(soundID, 1.0f, 1.0f, 1, 0, 1.0f)
            println("サウンド再生")
        }
    }


    fun close() { // シングルトンの場合呼びようがない？
        soundPool?.release()
        soundPool = null
    }
}