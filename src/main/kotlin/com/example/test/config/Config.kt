package com.example.test.config

import kotlinx.serialization.Serializable


@Serializable
data class Config(
    var pathToMusicFolder: String, // Path to music folder or server address(with music)
    val mountFTP: Boolean, // if true, then trying to mount with username:password
    val mountFolder: String, // folder with mounted music
    val username: String,
    val password: String,
    val dataFolder: String, // folder with playlist, config
    val bufferFolder: String, // /tmp/pmoc/ -- example
    val favoritesFolder: String, // folder with favorites music
    val playlistName: String, // dataFolder.resolve(playlistName) would be a playlist, if fiel not exists, then it will try to scan all music folder
)