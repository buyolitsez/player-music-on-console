package com.example.test.UI

import com.example.test.UI.Commands.*
import com.example.test.logger
import java.util.*

class ConsoleUI : UI {
    private val scanner = Scanner(System.`in`)

    override fun init(musicFolder: String) {
        logger.debug { "Path to music:$musicFolder" }
    }

    override fun songChanged(newSong: String) {
        logger.debug { "current song:$newSong" }
    }

    override fun songPauses() {
        logger.debug { "song paused!" }
    }

    override fun getUserCmd(): UserCommand {
        return when (scanner.next()) {
            "next" -> NextUserCommand()
            "play" -> PlayUserCommand()
            "pause" -> PauseUserCommand()
            "update" -> UpdateUserCommand()
            "delete" -> DeleteCurrentSongUserCommand()
            "exit" -> ExitUserCommand()
            else -> UnknownUserCommand()
        }
    }
}