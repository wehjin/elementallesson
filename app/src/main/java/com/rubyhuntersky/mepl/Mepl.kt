package com.rubyhuntersky.mepl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
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
        launch(Dispatchers.Main) {
            var exoPlayer: ExoPlayer? = null
            var clipBase = ""
            for (msg in channel) {
                when (msg) {
                    is Msg.PlayClip -> {
                        if (clipBase != msg.clipBase) {
                            exoPlayer?.release()
                            exoPlayer = null
                            clipBase = msg.clipBase
                        }
                        exoPlayer = exoPlayer?.also {
                            Log.d(tag, "Restarting player")
                            it.seekTo(0)
                        } ?: createExoPlayer(getClipUri(msg.clipBase), context)
                        exoPlayer.playWhenReady = true
                    }
                }
            }
            exoPlayer?.release()
        }
    }

    private fun createExoPlayer(uri: Uri, context: Context): ExoPlayer {
        return ExoPlayerFactory.newSimpleInstance(context).apply {
            addListener(object : Player.EventListener {
                override fun onPlayerError(error: ExoPlaybackException) {
                    throw error.sourceException
                }
            })

            val dataSourceFactory =
                DefaultDataSourceFactory(context, Util.getUserAgent(context, tag))
            val mediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            prepare(mediaSource)
        }
    }

    private fun getClipUri(clipBase: String): Uri {
        return Uri.parse("http://listen.rubyhuntersky.com/tts/${clipBase.trim()}_tts.mp3")
    }
}