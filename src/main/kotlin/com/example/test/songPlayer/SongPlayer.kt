import com.example.test.logger
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.File


class SongPlayer() {
    private lateinit var mediaPlayer: MediaPlayer
    private var volume = 30

    /**
     * 	Start/resume playing
     */
    fun play() {
        mediaPlayer.play()
    }

    /**
     * Pause playing
     */
    fun pause() {
        mediaPlayer.pause()
    }

    /**
     * Takes new songs and starts playing it
     */
    fun nextSong(song: File, endOfMedia: Runnable) {
        tryToStop()
        try {
            mediaPlayer = MediaPlayer(Media(song.toURI().toString()))
        } catch (e: Exception) {
            logger.error { "cant create player!!" }
            logger.error { "song = $song" }
            logger.error { "song.exists = ${song.exists()}" }
        }
        mediaPlayer.volume = volume / 100.0
        mediaPlayer.onEndOfMedia = endOfMedia
        play()
    }

    fun changeVolume(newVolume: Int) {
        require(newVolume in 0..100) { "New volume($newVolume) is out of range" }
        volume = newVolume
        mediaPlayer.volume = volume / 100.0
    }

    /**
     * Tries to stop media
     * Try/catch needed because mediaPlayer can be non initialized
     */
    fun tryToStop() {
        try {
            mediaPlayer.stop()
        } catch (e: Exception) {
            logger.debug { "Can't stop player, because: ${e.message}" }
        }
    }

    fun getMetadata(): javafx.collections.ObservableMap<String, Any> {
        return mediaPlayer.media.metadata
    }
}
