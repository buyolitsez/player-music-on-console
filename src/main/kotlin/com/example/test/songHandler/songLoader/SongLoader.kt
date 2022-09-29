package com.example.test.songHandler.songLoader

import com.example.test.config.Config
import java.io.File

abstract class SongLoader {
    var musicFolder: File
    lateinit var songs: List<File>

    constructor(config: Config, songs: List<File>) {
        this.musicFolder = File(config.pathToMusicFolder)
        this.songs = songs
    }

    constructor(config: Config) {
        this.musicFolder = File(config.pathToMusicFolder)
    }


    abstract suspend fun getNextSong(): File
    abstract suspend fun addToFavorite()
    abstract fun close()
    abstract fun deleteCurrentSong()
}