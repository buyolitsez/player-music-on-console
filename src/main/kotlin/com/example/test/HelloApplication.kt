package com.example.test


import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.*
import java.io.File
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

suspend fun getListOfSongs(dirPath: String, depthParallelize: Int): List<File> = coroutineScope {
    if (!File(dirPath).isDirectory) {
        listOf(File(dirPath))
    } else if (depthParallelize <= 0) {
        File(dirPath).walk().toList()
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
                totalFiles.add(file)
            }
        }
        totalFiles.addAll(listJobs.awaitAll().flatten())
        totalFiles
    }
}

@OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
fun main() {
    Platform.startup {
        val time = measureTime {
            runBlocking {
                val dirPath = "/run/user/1000/gvfs/ftp:host=164.92.142.157/additional/Music"
//        val dirPath = "1.mp3"
                assert(File(dirPath).exists())
                val depthParallelize = 2
                val listOfSongs = getListOfSongs(dirPath, depthParallelize)
                println("Total songs ${listOfSongs.size}")
            }
        }
        print("Takes $time")
        println("hmmmm")
        val bip = "1.mp3"
//        val bip = "/run/user/1000/gvfs/ftp:host=164.92.142.157/additional/Music/1.mp3"
        val hit = Media(File(bip).toURI().toString())
        println("1")
        val mediaPlayer = MediaPlayer(hit)
        println("2")
        val scanner = Scanner(System.`in`)
        println("ready!")
        GlobalScope.launch {
            var cmd = ""
            while (true) {
                cmd = scanner.next()
                if (cmd == "play") {
                    println("start play")
                    mediaPlayer.play()
                    println("after play")
                } else if (cmd == "pause") {
                    println("pause play")
                    mediaPlayer.pause()
                } else if (cmd == "stop") {
                    println("stop play")
                    mediaPlayer.stop()
                } else if (cmd == "vol") {
                    println("set volume:")
                    mediaPlayer.volume = scanner.nextInt() / 100.0
                }
            }
        }
    }
}