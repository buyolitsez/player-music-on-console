package com.example.test


import com.example.test.UI.Commands.*
import com.example.test.UI.ConsoleUI
import com.example.test.UI.UI
import com.example.test.config.Config
import com.example.test.config.ConfigReader
import com.example.test.songHandler.FTPSongsHandler
import com.example.test.songHandler.SongsHandler
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.system.exitProcess

lateinit var globalMediaPlayer: MediaPlayer

val logger = KotlinLogging.logger {}

val ui: UI = ConsoleUI()

fun tryToStop() {
    try {
        globalMediaPlayer.stop()
    } catch (_: Exception) {
    }
}

suspend fun genNew(songsHandler: SongsHandler): MediaPlayer {
    logger.debug { "gen new called!" }
    val newSong = songsHandler.getNextSong()
    val mediaPlayer = MediaPlayer(Media(newSong.toURI().toString()))
    mediaPlayer.onEndOfMedia = Runnable {
        tryToStop()
        globalMediaPlayer = runBlocking { genNew(songsHandler) }
        globalMediaPlayer.play()
    }
    ui.songChanged(newSong.absolutePath)
    return mediaPlayer
}

lateinit var config: Config

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val pathToConfig = "data/config.json"
    config = ConfigReader(pathToConfig).read()
    logger.debug { "Config: $config" }
    ui.init(config.pathToMusicFolder)
    val songsHandler: SongsHandler = FTPSongsHandler(config.pathToMusicFolder)

    Runtime.getRuntime().addShutdownHook(Thread(Runnable {
        logger.debug { "Shutdown" }
        songsHandler.close()
    }))
    Platform.startup {
        GlobalScope.launch {
            logger.debug { "player started!" }
            var cmd: UserCommand = NextUserCommand()
            while (true) {
                if (cmd is PlayUserCommand) {
                    logger.debug { "start play" }
                    globalMediaPlayer.play()
                    ui.continuePlaying()
                } else if (cmd is PauseUserCommand) {
                    logger.debug { "pause play" }
                    globalMediaPlayer.pause()
                    ui.songPaused()
                } else if (cmd is NextUserCommand) {
                    logger.debug { "next activated" }
                    tryToStop()
                    globalMediaPlayer = genNew(songsHandler)
                    globalMediaPlayer.play()
                } else if (cmd is UpdateUserCommand) {
                    songsHandler.loadSongFromDir(config.pathToMusicFolder)
                } else if (cmd is DeleteCurrentSongUserCommand) {
                    logger.debug { "Delete command detected" }
                    tryToStop()
                    songsHandler.deleteCurrentSong()
                    globalMediaPlayer = genNew(songsHandler)
                    globalMediaPlayer.play()
                } else if (cmd is ChangeVolumeUserCommand) {
                    val changeVolumeUserCommand = cmd
                    logger.debug { "Changing volume to ${changeVolumeUserCommand.newVolume}" }
                    globalMediaPlayer.volume = changeVolumeUserCommand.newVolume / 100.0
                    ui.volumeChanged(changeVolumeUserCommand.newVolume)
                } else if (cmd is AddToFavoriteUserCommand) {
                    songsHandler.addToFavorite()
                } else if (cmd is ExitUserCommand) {
                    tryToStop()
                    exitProcess(0)
                } else {
                    assert(cmd is UnknownUserCommand)
                    logger.debug { "Unknown command" }
                }
                cmd = ui.getUserCmd()
            }
        }
    }
}