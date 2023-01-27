package com.example.test


import com.example.test.UI.ConsoleUI
import com.example.test.UI.UI
import com.example.test.config.Config
import com.example.test.config.ConfigReader
import com.example.test.songHandler.FTPSongsHandler
import com.example.test.songHandler.SongsHandler
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.runMosaic
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
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

    if (config.mountFTP) { // TODO not working
        File(config.mountFolder).mkdirs()
        Runtime.getRuntime()
            .exec("umount ${config.mountFolder}")
        logger.info { "mounting folder ${config.pathToMusicFolder}" }
        Runtime.getRuntime()
            .exec("curlftpfs ${config.pathToMusicFolder} ${config.mountFolder} -o user=${config.username}:${config.password}")
        config.pathToMusicFolder = config.mountFolder
    }

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

//fun main() = runMosaic {
//    var count = 0
//
//    setContent {
//        Text("The count is: $count")
//    }
//
//    for (i in 1..20) {
//        delay(250)
//        count = i
//    }
//}
