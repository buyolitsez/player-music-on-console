package com.example.test.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


class ConfigReader(private val fileName: String) {
    fun read(): Config = Json.decodeFromString(File(fileName).readText())
}