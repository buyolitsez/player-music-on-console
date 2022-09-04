package com.example.test.commandHandler.Commands

class ChangeVolumeUserCommand() : UserCommand {
    var newVolume: Int = 100

    constructor(volume: Int) : this() {
        require(0 <= volume && volume <= 100) { "Volume should be integer between 0 and 100(inclusive)" }
        newVolume = volume
    }
}