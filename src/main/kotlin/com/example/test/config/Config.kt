package com.example.test.config

import kotlinx.serialization.Serializable


@Serializable
data class Config(
    val pathToMusicFolder: String,
    val bufferFolder: String,
    val favoritesFolder: String,
    val dataFolder: String,
    val playlistName: String,
)