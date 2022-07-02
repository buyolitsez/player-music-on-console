package com.example.test.UI.Commands

class ChangeVolumeUserCommand() : UserCommand {
    var newVolume: Double = 1.0

    constructor(volume: Int) : this() {
        require(0 <= volume && volume <= 100) { "Volume should be integer between 0 and 100(inclusive)" }
        newVolume = volume / 100.0
    }
}