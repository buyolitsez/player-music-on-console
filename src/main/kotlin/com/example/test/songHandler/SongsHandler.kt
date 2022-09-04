package com.example.test.songHandler

import java.io.File

interface SongsHandler {
    companion object {
        private val instance = mutableMapOf<String, SongsHandler>()
        fun getInstance(pathToDir: String): SongsHandler {
            if (!instance.containsKey(pathToDir)) {
                instance[pathToDir] = FTPSongsHandler(pathToDir)
            }
            return instance[pathToDir]!!
        }
    }

    /**
     * @param dirPath says that we want to get songs from that dir
     */
    fun loadSongFromDir(dirPath: String)

    suspend fun getNextSong(): File

    fun deleteCurrentSong()

    fun addToFavorite()

    fun close()
}