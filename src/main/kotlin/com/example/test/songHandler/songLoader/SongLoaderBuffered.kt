package com.example.test.songHandler.songLoader

import com.example.test.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.*
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
                    getListOfSongs(file, depthParallelize - 1)
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
    private val bufferFolder = File("/tmp/pmoc")
    private val buffer = Buffer()

    init {
        logger.debug { "Init part" }
        clearFolder(bufferFolder)
    }

    constructor(musicFolder: File, songs: List<File>) : super(musicFolder, songs) {
        logger.debug { "Build songs from list" }
    }

    constructor(musicFolder: File) : super(musicFolder) {
        logger.debug { "Build songs from directory:$musicFolder" }
        songs = runBlocking(Dispatchers.Default) { getListOfSongs(musicFolder) }
        logger.debug { "End build songs from dir" }
    }

    override suspend fun getNextSong(): File {
        var copied = buffer.downloadSong()
        logger.debug { "new song:$copied" }
        if (copied.extension == "flac") { // TODO
            logger.debug { "ooops flac!" }
            val toMp3 = File(copied.absolutePath.replace(".flac", ".mp3"))
            Runtime.getRuntime()
                .exec("ffmpeg -i ${copied.absolutePath} -ab 320k -map_metadata 0 -id3v2_version 3 ${toMp3.absolutePath}")
            logger.debug { "converted!" }
            copied = toMp3
        }
        return copied
    }

    private inner class Buffer {
        private val downloadedSongs = mutableListOf<File>()
        private val songsChannel = Channel<File>(UNLIMITED)
        private val BUFFER_SIZE = 5
        var currentSong: File? = null
        val mutex = Mutex()

        @OptIn(DelicateCoroutinesApi::class)
        suspend fun downloadSong(): File {
            GlobalScope.launch {
                mutex.withLock {
                    while (downloadedSongs.size < BUFFER_SIZE) {
                        val res = songs[(Random.nextUInt() % songs.size.toUInt()).toInt()]
                        val newFile =
                            bufferFolder.resolve(res.absolutePath.substringAfter(musicFolder.absolutePath).drop(1))
                        newFile.mkdirs()
                        newFile.delete()
                        logger.debug { "Downloading song:$newFile!\ncurrent size buffer:${downloadedSongs.size}" }
                        val downloaded = res.copyTo(newFile)
                        downloadedSongs.add(downloaded)
                        songsChannel.send(downloaded)
                    }
                }
                deleteLastDownloadedSong()
            }
            val newSong = songsChannel.receive()
            currentSong = musicFolder.resolve(newSong.absolutePath.substringAfter(bufferFolder.absolutePath).drop(1))
            return newSong
        }

        private suspend fun deleteLastDownloadedSong() {
            mutex.withLock {
                while (downloadedSongs.size >= BUFFER_SIZE) {
                    val toDelete = downloadedSongs[0]
                    logger.debug { "Deleting song:$toDelete, current size${downloadedSongs.size}" }
                    toDelete.delete()
                    downloadedSongs.removeAt(0)
                }
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