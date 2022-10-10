package com.example.test.commandHandler

import com.example.test.commandHandler.Commands.*
import java.util.*

class ScannerCommandHandler() : CommandHandler {
    private val scanner = Scanner(System.`in`)

    override fun getUserCommand(): UserCommand {
        return when (scanner.next()) {
            "next", "nx", "n" -> NextUserCommand()
            "p" -> SwitchPlayStatus()
            "update" -> UpdateUserCommand()
            "delete", "dl", "d" -> DeleteCurrentSongUserCommand()
            "volume", "v" -> ChangeVolumeUserCommand(scanner.nextInt())
            "add", "a" -> AddToFavouriteUserCommand()
            "exit" -> ExitUserCommand()
            else -> UnknownUserCommand()
        }
    }
}
