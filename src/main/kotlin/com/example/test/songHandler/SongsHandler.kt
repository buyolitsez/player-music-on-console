package com.example.test.songHandler

import com.example.test.config.Config
import java.io.File

interface SongsHandler {
    companion object {
        fun getInstance(config: Config): SongsHandler {
            return FTPSongsHandler(config)
        }
    }

    /**
     * @param dirPath says that we want to get songs from that dir
     */
    fun loadSongFromDir(dirPath: String)

    suspend fun getNextSong(): File

    fun deleteCurrentSong()

    fun addToFavourite()

    fun close()
}