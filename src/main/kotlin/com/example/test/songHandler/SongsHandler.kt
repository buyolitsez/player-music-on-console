package com.example.test.songHandler

import java.io.File

interface SongsHandler {
    fun loadSongFromDir(dirPath : String)
    fun getNextSong() : File

    fun close()
}