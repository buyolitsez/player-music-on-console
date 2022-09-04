package com.example.test.commandHandler

import com.example.test.commandHandler.Commands.UserCommand

interface CommandHandler {
    companion object {
        private val instance by lazy { ScannerCommandHandler() }
        fun getInstance(): CommandHandler = instance
    }

    fun getUserCommand(): UserCommand
}