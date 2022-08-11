package com.example.test.songHandler.songLoader

import java.io.File

abstract class SongLoader {
    var musicFolder: File
    lateinit var songs: List<File>

    constructor(musicFolder: File, songs: List<File>) {
        this.musicFolder = musicFolder
        this.songs = songs
    }

    constructor(musicFolder: File) {
        this.musicFolder = musicFolder
    }


    abstract suspend fun getNextSong(): File
    abstract suspend fun addToFavorite()
    abstract fun close()
    abstract fun deleteCurrentSong()
}