package com.example.test.UI

import com.example.test.UI.Commands.*
import java.util.*

class ConsoleUI : UI {
    private val scanner = Scanner(System.`in`)
    private val red = "\u001b[31m"
    private val resetColor = "\u001b[0m"

    private fun printlnRed(message: String) {
        println(red + message + resetColor)
    }

    override fun init(musicFolder: String) {
        printlnRed("Path to music:$musicFolder")
    }

    override fun songChanged(newSong: String) {
        printlnRed("Current song:$newSong")
    }

    override fun songPaused() {
        printlnRed("Song paused!")
    }

    override fun continuePlaying() {
        printlnRed("Continue playing!")
    }

    override fun volumeChanged(newVolume: Int) {
        printlnRed("New volume $newVolume!")
    }

    override fun getUserCmd(): UserCommand {
        return when (scanner.next()) {
            "next" -> NextUserCommand()
            "play" -> PlayUserCommand()
            "pause" -> PauseUserCommand()
            "update" -> UpdateUserCommand()
            "delete" -> DeleteCurrentSongUserCommand()
            "volume" -> ChangeVolumeUserCommand(scanner.nextInt())
            "add" -> AddToFavoriteUserCommand()
            "exit" -> ExitUserCommand()
            else -> UnknownUserCommand()
        }
    }
}