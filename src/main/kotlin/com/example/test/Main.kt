package com.example.test


import com.example.test.UI.UI
import com.example.test.commandHandler.Commands.NextUserCommand
import com.example.test.commandHandler.Commands.UserCommand
import com.example.test.config.ConfigReader
import javafx.application.Platform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File

val logger = KotlinLogging.logger {}

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    val pathToConfig = "data/config.json"
    val config = ConfigReader(pathToConfig).read()
    logger.debug { "Config: $config" }

    if (config.mountFTP) {
        TODO("not working")
        File(config.mountFolder).mkdirs()
        Runtime.getRuntime()
            .exec("umount ${config.mountFolder}")
        logger.info { "mounting folder ${config.pathToMusicFolder}" }
        Runtime.getRuntime()
            .exec("curlftpfs ${config.pathToMusicFolder} ${config.mountFolder} -o user=${config.username}:${config.password}")
        config.pathToMusicFolder = config.mountFolder
    }
    val ui = UI.getInstance()
    val controller = Controller(config, ui = ui)

    Runtime.getRuntime().addShutdownHook(Thread(Runnable {
        logger.debug { "Shutdown" }
        controller.close()
    }))
    Platform.startup {
        GlobalScope.launch {
            logger.debug { "player started!" }
            var cmd: UserCommand = NextUserCommand()
            while (true) {
                controller.executeUserCommand(cmd)
                cmd = ui.getUserCmd()
            }
        }
    }
}