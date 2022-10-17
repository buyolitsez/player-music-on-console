package com.example.test

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.*

class FileUtils {
    companion object {
        fun clearFolder(folder: File) {
            if (folder.exists()) {
                logger.debug { "deleting folder $folder" }
                assert(folder.deleteRecursively())
            }
            assert(folder.mkdir())
        }

        fun isSong(songPath: File): Boolean {
            if (songPath.isDirectory) {
                return false
            }
            return when (songPath.extension.lowercase(Locale.getDefault())) {
                "mp3", "flac" -> true
                "m3u", "jpg", "cue", "txt", "tif", "log", "jpeg", "docx", "m4a" -> false // TODO maybe m3u is playing
                else -> false.also { logger.error { "Unknown extension:${songPath.extension} on song${songPath.absolutePath}" } }
            }
        }

        suspend fun getListOfSongs(directory: File, depthParallelize: Int = 2): List<File> = coroutineScope {
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

        /**
         * exchangeFolder("abc/a.mp3", "abc", "kek") = "kek/a.mp3"
         */
        fun exchangeFolder(song: File, from: File, to: File): File {
            return to.resolve(song.absolutePath.substringAfter(from.absolutePath).drop(1))
        }
    }
}