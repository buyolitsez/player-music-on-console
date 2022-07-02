package com.example.test.songHandler

import com.example.test.logger
import com.example.test.songHandler.songLoader.SongLoader
import com.example.test.songHandler.songLoader.SongLoaderBuffered
import java.io.File

class FTPSongsHandler(pathToDir: String) : SongsHandler {
    private var listOfSongs: List<File> = listOf()
    private val dataFolder = File("data/")
    private val playlist = dataFolder.resolve("playlist")
    private lateinit var songLoader: SongLoader
    private lateinit var dirPath: String

    init {
        dataFolder.mkdir()
        var playlistReaded = false
        if (playlist.exists()) {
            val lines = playlist.readLines()
            dirPath = lines[0]
            logger.debug { "dirPath:$dirPath" }
            logger.debug { "reading playlist:$playlist" }
            listOfSongs = lines.drop(1).map {
                File(it)
            }
            songLoader = SongLoaderBuffered(File(dirPath), listOfSongs)
            playlistReaded = listOfSongs.isNotEmpty()
        }
        if (!playlistReaded) {
            logger.debug { "No playlist was read, starting scanning for songs" }
            dirPath = pathToDir
            loadSongFromDir(dirPath)
        }
    }

    override fun loadSongFromDir(dirPath: String) {
        this.dirPath = dirPath
        songLoader = SongLoaderBuffered(File(dirPath))
        this.listOfSongs = songLoader.songs
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
            logger.debug { "Adding song ${it.absolutePath} to playlist" }
            builder.append("${it.absolutePath}\n")
        }
        playlist.writeText(builder.toString())
    }
}