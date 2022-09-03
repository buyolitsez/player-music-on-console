package com.example.test.UI

import com.example.test.UI.Commands.UserCommand
import javafx.scene.media.MediaPlayer

interface UI {
    fun init(musicFolder: String)
    fun songChanged(newSong: String, mediaPlayer: MediaPlayer)
    fun songPaused()
    fun continuePlaying()
    fun volumeChanged(newVolume: Int)
    fun getUserCmd(): UserCommand
}