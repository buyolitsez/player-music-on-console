package com.example.test.songHandler

import com.example.test.logger
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.random.Random
import kotlin.random.nextUInt

class FTPSongsHandler : SongsHandler {
    private var listOfSongs: List<File> = listOf()
    private val bufferFolder = File("/tmp/pmoc")

    init {
        if (bufferFolder.exists()) {
            logger.debug { "deleting buffer folder" }
            assert(bufferFolder.deleteRecursively())
        }
        assert(bufferFolder.mkdir())
    }

    override fun loadSongFromDir(dirPath: String) {
        logger.debug { "start get from dir" }
        listOfSongs = runBlocking { getListOfSongs(dirPath) }
        logger.debug { "end get from dir" }
    }

    override fun getNextSong(): File {
        val res = listOfSongs[(Random.nextUInt() % listOfSongs.size.toUInt()).toInt()]
        var copied = res.copyTo(bufferFolder.resolve(res.name))
        logger.debug { "new song:$copied" }
        if (copied.extension == "flac") {
            logger.debug { "ooops flac!" }
            val toMp3 = File(copied.absolutePath.replace(".flac", ".mp3"))
            Runtime.getRuntime()
                .exec("ffmpeg -i ${copied.absolutePath} -ab 320k -map_metadata 0 -id3v2_version 3 ${toMp3.absolutePath}")
            logger.debug { "converted!" }
            copied = toMp3
        }
        return copied
    }

    override fun close() {
        bufferFolder.deleteRecursively()
    }

    private suspend fun getListOfSongs(dirPath: String, depthParallelize: Int = 2): List<File> = coroutineScope {
        if (!File(dirPath).isDirectory) {
            logger.debug { "single thread walk(((" }
            listOf(File(dirPath))
        } else if (depthParallelize <= 0) {
            File(dirPath).walk().toList().filter { isSong(it) }
        } else {
            val listJobs = mutableListOf<Deferred<List<File>>>()
            val totalFiles = mutableListOf<File>()
            File(dirPath).walk().maxDepth(depthParallelize).forEach { file ->
                val depthDir = dirPath.count { it == '/' }
                if (file.isDirectory && file.absolutePath.count { it == '/' } == depthDir + depthParallelize) {
                    listJobs.add(async {
                        val currDir = file
                        getListOfSongs(currDir.absolutePath, depthParallelize - 1)
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
}