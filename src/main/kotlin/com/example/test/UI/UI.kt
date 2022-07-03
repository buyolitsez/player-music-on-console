package com.example.test.UI

import com.example.test.UI.Commands.UserCommand

interface UI {
    fun init(musicFolder: String)
    fun songChanged(newSong: String)
    fun songPaused()
    fun continuePlaying()
    fun volumeChanged(newVolume: Int)
    fun getUserCmd(): UserCommand
}