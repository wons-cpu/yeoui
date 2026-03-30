package com.yeoui.player

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

/**
 * SpotifyRemoteManager
 *
 * A singleton wrapper around Spotify App Remote SDK that manages
 * the connection lifecycle and provides simplified playback controls.
 *
 * Usage:
 *   SpotifyRemoteManager.connect(context) { connected ->
 *       if (connected) SpotifyRemoteManager.play("spotify:track:xxxx")
 *   }
 */
object SpotifyRemoteManager {

    private const val TAG = "SpotifyRemoteManager"

    // TODO: Move to BuildConfig via local.properties
    private const val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID"
    private const val REDIRECT_URI = "yeoui://callback"

    private var spotifyAppRemote: SpotifyAppRemote? = null

    val isConnected: Boolean
        get() = spotifyAppRemote?.isConnected == true

    // ─────────────────────────────────────────────
    // Connection Lifecycle
    // ─────────────────────────────────────────────

    fun connect(context: Context, onResult: (Boolean) -> Unit) {
        val params = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, params, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d(TAG, "Spotify App Remote connected successfully")
                onResult(true)
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "Spotify connection failed", throwable)
                onResult(false)
            }
        })
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote = null
            Log.d(TAG, "Spotify App Remote disconnected")
        }
    }

    // ─────────────────────────────────────────────
    // Playback Controls
    // ─────────────────────────────────────────────

    /**
     * Play a Spotify URI (track, album, or playlist).
     * @param spotifyUri e.g. "spotify:track:6rqhFgbbKwnb9MLmUQDhG6"
     */
    fun play(spotifyUri: String) {
        spotifyAppRemote?.playerApi?.play(spotifyUri)
            ?: Log.w(TAG, "Cannot play — not connected")
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun skipNext() {
        spotifyAppRemote?.playerApi?.skipNext()
    }

    fun skipPrevious() {
        spotifyAppRemote?.playerApi?.skipPrevious()
    }

    /**
     * Add a track to the playback queue.
     * Used by the recommendation engine to line up context-matched songs.
     */
    fun queue(spotifyUri: String) {
        spotifyAppRemote?.playerApi?.queue(spotifyUri)
            ?: Log.w(TAG, "Cannot queue — not connected")
    }

    // ─────────────────────────────────────────────
    // Player State (for Context Engine feedback)
    // ─────────────────────────────────────────────

    /**
     * Subscribe to player state changes.
     * The Context Engine uses this to detect skips vs. full listens
     * for Music Map logging.
     */
    fun subscribeToPlayerState(onStateChanged: (PlayerStateInfo) -> Unit) {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val info = PlayerStateInfo(
                trackUri = playerState.track?.uri.orEmpty(),
                trackName = playerState.track?.name.orEmpty(),
                artistName = playerState.track?.artist?.name.orEmpty(),
                isPaused = playerState.isPaused,
                playbackPositionMs = playerState.playbackPosition
            )
            onStateChanged(info)
        }
    }

    /**
     * Lightweight data class exposing only the fields
     * the rest of the app needs from Spotify's PlayerState.
     */
    data class PlayerStateInfo(
        val trackUri: String,
        val trackName: String,
        val artistName: String,
        val isPaused: Boolean,
        val playbackPositionMs: Long
    )
}
