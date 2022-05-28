package com.example.test.UI

import com.example.test.UI.Commands.UserCommand

interface UI {
    fun init(musicFolder: String)
    fun songChanged(newSong: String)
    fun songPauses()
    fun getUserCmd() : UserCommand
}