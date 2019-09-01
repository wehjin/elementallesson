package com.rubyhuntersky.mepl

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object Mepl : CoroutineScope {
    private val tag = Mepl::class.java.simpleName
    private val job = Job()
    override val coroutineContext: CoroutineContext = job

    sealed class Msg {
        data class PlayClip(val clipBase: String) : Msg()
    }

    private lateinit var channel: Channel<Msg>

    fun playClip(clipBase: String) {
        channel.offer(Msg.PlayClip(clipBase))
    }

    fun stop() {
        job.cancel()
        channel.close()
    }

    fun start(context: Context) {
        channel = Channel(10)
        launch(Dispatchers.IO) {
            var mediaPlayer: MediaPlayer? = null
            var clipBase = ""
            for (msg in channel) {
                when (msg) {
                    is Msg.PlayClip -> {
                        if (clipBase != msg.clipBase) {
                            mediaPlayer?.release()
                            mediaPlayer = null
                            clipBase = msg.clipBase
                        }
                        mediaPlayer = mediaPlayer?.also {
                            it.seekTo(0)
                            it.start()
                            Log.d(tag, "Restarted player")
                        } ?: createMediaPlayer(getClipUri(msg.clipBase), context)
                    }
                }
            }
        }
    }

    private fun createMediaPlayer(uri: Uri, context: Context): MediaPlayer {
        Log.d(tag, "URI: $uri")
        return MediaPlayer.create(context, uri).apply {
            setOnErrorListener { _, _, extra ->
                val message = when (extra) {
                    MediaPlayer.MEDIA_ERROR_IO -> "MEDIA_ERROR_IO"
                    MediaPlayer.MEDIA_ERROR_MALFORMED -> "MEDIA_ERROR_MALFORMED"
                    MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK"
                    MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MEDIA_ERROR_SERVER_DIED"
                    MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "MEDIA_ERROR_TIMED_OUT"
                    MediaPlayer.MEDIA_ERROR_UNKNOWN -> "MEDIA_ERROR_UNKNOWN"
                    MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "MEDIA_ERROR_UNSUPPORTED"
                    else -> extra.toString()
                }
                Log.e(tag, "ERROR: $message")
                true
            }
            start()
            Log.i(tag, "Created player")
        }
    }

    private fun getClipUri(clipBase: String): Uri {
        return Uri.parse("http://listen.rubyhuntersky.com/tts/${clipBase.trim()}_tts.mp3")
    }
}