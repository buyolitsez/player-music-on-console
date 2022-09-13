package com.example.test.songHandler.songLoader

import com.example.test.config
import com.example.test.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.random.nextUInt

private fun clearFolder(folder: File) {
    if (folder.exists()) {
        logger.debug { "deleting buffer folder" }
        assert(folder.deleteRecursively())
    }
    assert(folder.mkdir())
}

private fun isSong(songPath: File): Boolean {
    if (songPath.isDirectory) {
        return false
    }
    return when (songPath.extension.lowercase(Locale.getDefault())) {
        "mp3", "flac", "m3u" -> true
        "jpg", "cue", "txt", "tif", "log", "jpeg", "docx" -> false
        else -> throw java.lang.Exception("Unknown extension:${songPath.extension} on song${songPath.absolutePath}")
    }
}

private suspend fun getListOfSongs(directory: File, depthParallelize: Int = 2): List<File> = coroutineScope {
    if (!directory.isDirectory) {
        logger.debug { "single thread walk(((" }
        listOf(directory)
    } else if (depthParallelize <= 0) {
        directory.walk().toList().filter { isSong(it) }
    } else {
        val listJobs = mutableListOf<Deferred<List<File>>>()
        val totalFiles = mutableListOf<File>()
        directory.walk().maxDepth(depthParallelize).forEach { file ->
            val depthDir = directory.absolutePath.count { it == '/' }
            if (file.isDirectory && file.absolutePath.count { it == '/' } == depthDir + depthParallelize) {
                listJobs.add(async {
                    getListOfSongs(file, 0)
                })
            } else {
                if (isSong(file)) {
                    totalFiles.add(file)
                }
            }
        }
        totalFiles.addAll(listJobs.awaitAll().flatten())
        totalFiles
    }
}

class SongLoaderBuffered : SongLoader {
    private val bufferFolder = File(config.bufferFolder)
    private val buffer = Buffer()
    private val favoritesFolder = File(config.favoritesFolder)

    init {
        logger.debug { "Init part" }
        clearFolder(bufferFolder)
        favoritesFolder.mkdirs()
    }

    constructor(musicFolder: File, songs: List<File>) : super(musicFolder, songs) {
        logger.debug { "Build songs from list" }
    }

    constructor(musicFolder: File) : super(musicFolder) {
        logger.debug { "Build songs from directory:$musicFolder" }
        songs = runBlocking(Dispatchers.Default) { getListOfSongs(musicFolder) }
        logger.debug { "End build songs from dir, found ${songs.size} songs" }
    }

    override suspend fun getNextSong(): File {
        var copied = buffer.downloadSong()
        logger.debug { "new song:$copied" }
        if (copied.extension == "flac") { // TODO
            assert(false)
            logger.debug { "ooops flac!" }
            val toMp3 = File(copied.absolutePath.replace(".flac", ".mp3"))
            Runtime.getRuntime()
                .exec("ffmpeg -i ${copied.absolutePath} -ab 320k -map_metadata 0 -id3v2_version 3 ${toMp3.absolutePath}")
            logger.debug { "converted!" }
            copied = toMp3
        }
        return copied
    }

    /**
     * exchangeFolder("abc/a.mp3", "abc", "kek") = "kek/a.mp3"
     */
    private fun exchangeFolder(song: File, from: File, to: File): File {
        return to.resolve(song.absolutePath.substringAfter(from.absolutePath).drop(1))
    }

    override suspend fun addToFavorite() {
        val song = buffer.currentSong
        logger.debug { "Add song $song to favorites" }
        require(song != null) { "Song is null!" }
        val songNewPlace = exchangeFolder(song, musicFolder, favoritesFolder)
        if (!songNewPlace.exists()) {
            songNewPlace.mkdirs()
            songNewPlace.delete()
            song.copyTo(songNewPlace)
        }
    }

    private inner class Buffer {
        private val songsChannel = Channel<File>(UNLIMITED)
        private val songsInChannel = AtomicInteger(0)
        private val bufferSize = 5
        var currentSong: File? = null
        private var previousSong: File? = null

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun downloadSong(): File {
            GlobalScope.launch {
                while (songsInChannel.get() < bufferSize) {
                    val res = songs[(Random.nextUInt() % songs.size.toUInt()).toInt()]
					if (!res.exists()) {
						logger.debug { "Song $res not exists, skip"}
						continue;
					}
                    val newFile = exchangeFolder(res, musicFolder, bufferFolder)
                    newFile.mkdirs()
                    newFile.delete()
                    songsInChannel.incrementAndGet()
                    logger.debug { "Downloading song:$newFile!\ncurrent size buffer:${songsInChannel.get()}" }
                    val downloaded = res.copyTo(newFile)
                    songsChannel.send(downloaded)
                }
            }
            val newSong = songsChannel.receive()
            currentSong = exchangeFolder(newSong, bufferFolder, musicFolder)
            deleteSong(newSong)
            songsInChannel.decrementAndGet()
            return newSong
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun deleteSong(song: File) {
            GlobalScope.launch {
                previousSong?.let {
                    logger.debug { "Deleting song:$it" }
                    it.delete()
                }
                previousSong = song
            }
        }
    }

    override fun close() {
        bufferFolder.deleteRecursively()
    }

    override fun deleteCurrentSong() {
        buffer.currentSong?.let {
            logger.debug { "Deleting $it from server" }
            it.delete()
        }
        buffer.currentSong = null
    }
}
