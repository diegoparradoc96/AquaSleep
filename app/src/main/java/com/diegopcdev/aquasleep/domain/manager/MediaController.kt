package com.diegopcdev.aquasleep.domain.manager

import android.content.Context
import android.media.AudioManager

class MediaController(private val context: Context) {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun pauseCurrentMedia() {
        // Solicitar foco de audio para pausar YouTube y otras apps de media
        audioManager.requestAudioFocus(
            null, // No necesitamos listener
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }
}
