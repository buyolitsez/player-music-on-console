package com.example.test.songHandler

import com.example.test.logger
import com.example.test.songHandler.songLoader.SongLoader
import com.example.test.songHandler.songLoader.SongLoaderBuffered
import java.io.File

class FTPSongsHandler : SongsHandler {
    private var listOfSongs: List<File> = listOf()
    private val dataFolder = File("data/")
    private val playlist = dataFolder.resolve("playlist")
    private lateinit var songLoader: SongLoader
    private lateinit var dirPath: String

    init {
        dataFolder.mkdir()
        if (playlist.exists()) {
            val lines = playlist.readLines()
            dirPath = lines[0]
            logger.debug { "dirPath:$dirPath" }
            logger.debug { "reading playlist:$playlist" }
            listOfSongs = lines.drop(1).map {
                File(it)
            }
            songLoader = SongLoaderBuffered(File(dirPath), listOfSongs)
        }
    }

    override fun loadSongFromDir(dirPath: String) {
        this.dirPath = dirPath
        songLoader = SongLoaderBuffered(File(dirPath))
    }

    override suspend fun getNextSong(): File {
        return songLoader.getNextSong()
    }

    override fun deleteCurrentSong() {
        songLoader.deleteCurrentSong()
    }

    override fun close() {
        logger.debug { "close FTPSongsHandler" }
        songLoader.close()

        val builder = StringBuilder()
        builder.append("$dirPath\n")
        listOfSongs.forEach {
            builder.append("${it.absolutePath}\n")
        }
        playlist.writeText(builder.toString())
    }
}