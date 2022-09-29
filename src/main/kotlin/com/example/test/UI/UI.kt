package com.example.test.UI

import SongPlayer
import com.example.test.commandHandler.Commands.UserCommand

interface UI {
    companion object {
        private val instance by lazy { ConsoleUI() }
        fun getInstance(): UI = instance
    }

    /**
     * Calls on player initialization
     */
    fun init(musicFolder: String)

    /**
     * Calls on each song's changes(not after resume or pause)
     */
    fun songChanged(newSong: String, songPlayer: SongPlayer)

    /**
     * Calls on songs pause
     */
    fun songPaused()

    /**
     * Calls on songs resume
     */
    fun continuePlaying()

    /**
     * @param newVolume should be in [0, 100]
     */
    fun volumeChanged(newVolume: Int)

    fun getUserCmd(): UserCommand
}