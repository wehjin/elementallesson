package com.rubyhuntersky.mepl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.rubyhuntersky.data.ClipBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

object Mepl : CoroutineScope {
    private val tag = Mepl::class.java.simpleName
    private val job = Job()
    override val coroutineContext: CoroutineContext = job

    sealed class Msg {
        data class PlayClip(val clipBase: ClipBase) : Msg()
    }

    private lateinit var channel: Channel<Msg>

    fun playClip(clipBase: ClipBase) {
        channel.offer(Msg.PlayClip(clipBase))
    }

    fun stop() {
        job.cancel()
        channel.close()
    }

    fun start(context: Context) {
        channel = Channel(10)
        val cache = makeCache(context)
        launch(Dispatchers.Main) {
            var exoPlayer: ExoPlayer? = null
            var clipBase: ClipBase? = null
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
                        } ?: createExoPlayer(getClipUri(msg.clipBase), context, cache)
                        exoPlayer.playWhenReady = true
                    }
                }
            }
            exoPlayer?.release()
        }
    }

    private fun createExoPlayer(uri: Uri, context: Context, cache: Cache): ExoPlayer {
        return ExoPlayerFactory.newSimpleInstance(context).apply {
            addListener(object : Player.EventListener {
                override fun onPlayerError(error: ExoPlaybackException) {
                    throw error.sourceException
                }
            })
            val dataSourceFactory =
                DefaultDataSourceFactory(context, Util.getUserAgent(context, tag))
            val cacheDataSourceFactory = CacheDataSourceFactory(cache, dataSourceFactory)
            val mediaSource =
                ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri)
            prepare(mediaSource)
        }
    }

    private fun makeCache(context: Context): SimpleCache {
        val cacheDir = context.cacheDir
        val evictor = LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100)
        val databaseProvider = ExoDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }

    private fun getClipUri(clipBase: ClipBase) = Uri.parse(clipBase.toUrlString())

    private fun ClipBase.toUrlString(): String = when (this) {
        is ClipBase.Raw -> {
            val query = URLEncoder.encode(phrase, "UTF-8")
            "http://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&q=${query}&tl=ja"
        }
        is ClipBase.Symbolic -> {
            "http://listen.rubyhuntersky.com/tts/${symbol}_tts.mp3"
        }
    }
}