package com.example.test

import SongPlayer
import com.example.test.UI.UI
import com.example.test.commandHandler.Commands.*
import com.example.test.config.Config
import com.example.test.songHandler.SongsHandler
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import kotlin.system.exitProcess

class Controller(
    private val config: Config,
    private val songsHandler: SongsHandler = SongsHandler.getInstance(config),
    private val ui: UI = UI.getInstance(),
) : Closeable {

    private val songPlayer = SongPlayer()

    init {
        ui.init(config.pathToMusicFolder)
    }

    suspend fun executeUserCommand(cmd: UserCommand) {
        logger.debug { "New command detected: ${cmd::class}" }
        when (cmd) {
            is PlayUserCommand -> {
                logger.debug { "start play" }
                songPlayer.play()
                ui.continuePlaying()
            }
            is PauseUserCommand -> {
                logger.debug { "pause play" }
                songPlayer.pause()
                ui.songPaused()
            }
            is NextUserCommand -> {
                logger.debug { "next activated" }
                val newSong = songsHandler.getNextSong()
                songPlayer.nextSong(newSong) {
                    runBlocking { executeUserCommand(NextUserCommand()) }
                }
                ui.songChanged(newSong.absolutePath, songPlayer)
            }
            is UpdateUserCommand -> {
                logger.debug { "Update command detected" }
                songsHandler.loadSongFromDir(config.pathToMusicFolder)
            }
            is DeleteCurrentSongUserCommand -> {
                logger.debug { "Delete command detected" }
                songPlayer.tryToStop()
                songsHandler.deleteCurrentSong()
                executeUserCommand(NextUserCommand())
            }
            is ChangeVolumeUserCommand -> {
                logger.debug { "Changing volume to ${cmd.newVolume}" }
                songPlayer.changeVolume(cmd.newVolume)
                ui.volumeChanged(cmd.newVolume)
            }
            is AddToFavouriteUserCommand -> {
                logger.debug { "Add to favourite user command detected" }
                songsHandler.addToFavourite()
            }
            is ExitUserCommand -> {
                logger.debug { "Exit command detected" }
                songPlayer.tryToStop()
                exitProcess(0)
            }
            else -> {
                logger.debug { "Unknown command detected" }
                assert(cmd is UnknownUserCommand)
            }
        }
    }

    override fun close() {
        songsHandler.close()
    }

}