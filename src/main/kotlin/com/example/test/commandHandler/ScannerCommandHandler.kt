package com.example.test.commandHandler

import com.example.test.commandHandler.Commands.*
import java.util.*

class ScannerCommandHandler() : CommandHandler {
    private val scanner = Scanner(System.`in`)

    override fun getUserCommand(): UserCommand {
        return when (scanner.next()) {
            "next", "nx", "n", "т" -> NextUserCommand()
            "p", "з" -> SwitchPlayStatus()
            "update" -> UpdateUserCommand()
            "delete", "dl", "d", "в" -> DeleteCurrentSongUserCommand()
            "volume", "v", "м" -> ChangeVolumeUserCommand(scanner.nextInt())
            "add", "a", "ф" -> AddToFavouriteUserCommand()
            "exit" -> ExitUserCommand()
            else -> UnknownUserCommand()
        }
    }
}
