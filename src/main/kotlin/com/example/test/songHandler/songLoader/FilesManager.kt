package com.example.test.songHandler.songLoader

import com.example.test.FileUtils
import com.example.test.logger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class FilesManager(
    private val bufferFolder: File,
    private val musicFolder: File
) {
    @OptIn(DelicateCoroutinesApi::class)
    fun downloadFile(file: File): File {
        GlobalScope.launch {
            val newFile = FileUtils.exchangeFolder(file, musicFolder, bufferFolder)
            if (!newFile.exists()) {
                newFile.mkdirs()
                logger.debug { "Downloading file:$newFile!" }
                file.copyTo(newFile)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun deleteSong(song: File) {
        GlobalScope.launch {
            logger.debug { "Deleting file:$song" }
            song.delete()
        }
    }
}