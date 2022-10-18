package com.example.test.songHandler.songLoader

import com.example.test.config.Config
import com.example.test.logger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FilesIterator(
    private val config: Config,
    private val files: List<File>
) {
    /**
     * We keep three iterators, they represent a range of buffered songs an a position of current song, for example
     * [
     * 0, -- [rightFilePos]
     * 1,
     * 2, -- [leftFilePos]
     * 3, -- [currentFilePos]
     * 4
     * ]
     * Means that songs [2, 3, 4, 0] are buffered and current song is number 3.
     * Keep invariant that [currentFilePos] is inside buffered range
     *
     * [leftGap] and [rightGap] means that we want to have distance between left and current is about [leftGap] and distance
     * between current and right is about [rightGap]
     */
    private var currentFilePos = 0
    private var leftFilePos = 0
    private var rightFilePos = 0
    private val leftGap = 3
    private val rightGap = 7

    private val filesManager = FilesManager(config.bufferFolder, config.pathToMusicFolder)

    /**
     * @param files List of files
     * @param needToShuffle Is shuffle needed?
     */
    constructor(config: Config, files: List<File>, needToShuffle: Boolean) : this(
        config,
        if (needToShuffle) files.shuffled() else files
    ) {
        if (leftGap + rightGap + 1 + 2 >= files.size) {
            logger.error { "Too little files, errors may occur! It's recommended to have at least ${leftGap + rightGap + 1 + 2} files" }
        }
        moveIterators()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun moveIterators() {
        GlobalScope.launch {
            synchronized(this) {
                while (oneMove()) {
                }
            }
        }
    }

    @Synchronized
    private fun oneMove(): Boolean =
        if (distance(currentFilePos, rightFilePos) < rightGap) {
            logger.debug { "move right +1" }
            rightFilePos = oneMoveAndFunc(rightFilePos, this::increase, filesManager::downloadFile, true)
            true
        } else if (distance(leftFilePos, currentFilePos) < leftGap) {
            logger.debug { "move left -1" }
            leftFilePos = oneMoveAndFunc(leftFilePos, this::decrease, filesManager::downloadFile, true)
            true
        } else if (distance(currentFilePos, rightFilePos) > rightGap) {
            logger.debug { "move right -1" }
            rightFilePos = oneMoveAndFunc(rightFilePos, this::decrease, filesManager::deleteSong, false)
            true
        } else if (distance(leftFilePos, currentFilePos) > leftGap) {
            logger.debug { "move left +1" }
            leftFilePos = oneMoveAndFunc(leftFilePos, this::increase, filesManager::deleteSong, false)
            true
        } else {
            false
        }

    private fun oneMoveAndFunc(pos: Int, move: (Int) -> Int, op: (File) -> Unit, opWithNew: Boolean): Int {
        val newPos = move(pos)
        if (opWithNew)
            op(files[newPos])
        else
            op(files[pos])
        return newPos
    }

    private fun increase(pos: Int): Int {
        return (pos + 1) % files.size
    }

    private fun decrease(pos: Int): Int {
        return (pos + 1) % files.size
    }

    private fun distance(left: Int, right: Int): Int {
        if (left < right) {
            return right - left
        }
        return left + (files.size - right)
    }
}