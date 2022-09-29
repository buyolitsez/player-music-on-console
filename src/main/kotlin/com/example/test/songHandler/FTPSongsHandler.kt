package com.example.test.songHandler

import com.example.test.config.Config
import com.example.test.logger
import com.example.test.songHandler.songLoader.SongLoader
import com.example.test.songHandler.songLoader.SongLoaderBuffered
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FTPSongsHandler(private val config: Config) : SongsHandler {
    private var listOfSongs: List<File> = listOf()
    private val dataFolder = File(config.dataFolder)
    private val playlist = dataFolder.resolve(config.playlistName)
    private lateinit var songLoader: SongLoader
    private lateinit var dirPath: String

    init {
        dataFolder.mkdir()
        var playlistRead = false
        if (playlist.exists()) {
            val lines = playlist.readLines()
            dirPath = lines[0]
            logger.debug { "dirPath:$dirPath" }
            logger.debug { "reading playlist:$playlist" }
            listOfSongs = lines.drop(1).map {
                File(it)
            }
            songLoader = SongLoaderBuffered(config, listOfSongs)
            playlistRead = listOfSongs.isNotEmpty()
        }
        if (!playlistRead) {
            logger.debug { "No playlist was read, starting scanning for songs" }
            dirPath = config.pathToMusicFolder
            loadSongFromDir(dirPath)
        }
    }

    override fun loadSongFromDir(dirPath: String) {
        this.dirPath = dirPath
        songLoader = SongLoaderBuffered(config)
        this.listOfSongs = songLoader.songs
    }

    override suspend fun getNextSong(): File {
        var nextSong = songLoader.getNextSong()
        while (!nextSong.exists()) {
            logger.warn { "Tried to load non-existing song $nextSong" }
            nextSong = songLoader.getNextSong()
        }
        return nextSong
    }

    override fun deleteCurrentSong() {
        songLoader.deleteCurrentSong()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToFavourite() {
        GlobalScope.launch {
            songLoader.addToFavorite()
        }
    }

    override fun close() {
        logger.debug { "close FTPSongsHandler" }
        songLoader.close()

        val builder = StringBuilder()
        builder.append("$dirPath\n")
        listOfSongs.forEach {
            builder.append("${it.absolutePath}\n")
        }
        logger.debug { "Saved ${listOfSongs.size} songs" }
        playlist.writeText(builder.toString())
    }
}