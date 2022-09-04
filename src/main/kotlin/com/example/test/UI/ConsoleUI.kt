package com.example.test.UI

import com.example.test.commandHandler.CommandHandler
import com.example.test.commandHandler.Commands.UserCommand
import javafx.scene.media.MediaPlayer

class ConsoleUI : UI {
    private val red = "\u001b[31m"
    private val resetColor = "\u001b[0m"

    private fun printlnRed(message: String) {
        println(red + message + resetColor)
    }

    override fun init(musicFolder: String) {
        printlnRed("Path to music:$musicFolder")
    }

    override fun songChanged(newSong: String, mediaPlayer: MediaPlayer) {
        val metadata = mediaPlayer.media.metadata
        val artist = metadata["artist"]
        val year = metadata["year"]
        val album = metadata["album"]
        val genre = metadata["genre"]
        val title = metadata["title"]
        metadata.forEach { t, u ->
            println(t + "#$#" + u)
        }
        printlnRed(newSong)
        printlnRed("$artist - $title | $album | $year | $genre")
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
        return CommandHandler.getInstance().getUserCommand()
    }
}