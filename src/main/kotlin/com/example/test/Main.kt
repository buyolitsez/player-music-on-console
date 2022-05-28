package com.example.test


import com.example.test.UI.Commands.*
import com.example.test.UI.ConsoleUI
import com.example.test.UI.UI
import com.example.test.songHandler.FTPSongsHandler
import com.example.test.songHandler.SongsHandler
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

lateinit var globalMediaPlayer: MediaPlayer

val logger = KotlinLogging.logger {}

fun tryToStop() {
    try {
        globalMediaPlayer.stop()
    } catch (_: Exception) {
    }
}

fun genNew(songsHandler: SongsHandler): MediaPlayer {
    logger.debug { "gen new called!" }
    val mediaPlayer = MediaPlayer(Media(songsHandler.getNextSong().toURI().toString()))
    mediaPlayer.onEndOfMedia = Runnable {
        tryToStop()
        globalMediaPlayer = genNew(songsHandler)
        globalMediaPlayer.play()
    }
    return mediaPlayer
}

@OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
fun main() {
    Platform.startup {
        val dirPath = "/run/user/1000/gvfs/ftp:host=164.92.142.157/additional/Music"
        val songsHandler: SongsHandler = FTPSongsHandler()
        val ui: UI = ConsoleUI()
        GlobalScope.launch {
            logger.debug { "player started!" }
            var cmd: UserCommand
            while (true) {
                cmd = ui.getUserCmd()
                if (cmd is PlayUserCommand) {
                    logger.debug { "start play" }
                    globalMediaPlayer.play()
                } else if (cmd is PauseUserCommand) {
                    logger.debug { "pause play" }
                    globalMediaPlayer.pause()
                } else if (cmd is NextUserCommand) {
                    logger.debug { "next activated" }
                    tryToStop()
                    globalMediaPlayer = genNew(songsHandler)
                    globalMediaPlayer.play()
                } else if (cmd is UpdateUserCommand) {
                    songsHandler.loadSongFromDir(dirPath)
                } else if (cmd is ExitUserCommand) {
                    tryToStop()
                    break
                } else {
                    assert(cmd is UnknownUserCommand)
                }
            }
            songsHandler.close()
        }
    }
}