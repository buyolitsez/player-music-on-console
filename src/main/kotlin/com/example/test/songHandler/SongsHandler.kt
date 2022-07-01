package com.example.test.songHandler

import java.io.File

interface SongsHandler {
    fun loadSongFromDir(dirPath: String)
    suspend fun getNextSong(): File

    fun deleteCurrentSong()

    fun close()
}