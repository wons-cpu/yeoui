package com.yeoui.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MusicContextEntity
 *
 * Room database entity that logs every track playback with its
 * driving context snapshot. This is the foundation of the user's
 * personal "Music Map" — a context-tagged listening history that
 * powers future recommendations and social sharing.
 *
 * Each row = one song played in one specific driving context.
 *
 * Example row:
 *   trackId = "4iV5W9uYEdYUVa79Axb7Rh"
 *   speed = 65.0 (mph)
 *   weather = "clear"
 *   timeOfDay = "night"
 *   wasSkipped = false
 *   → User enjoyed this track while cruising at 65 mph on a clear night.
 */
@Entity(tableName = "music_context")
data class MusicContextEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ── Track Info ──────────────────────────────
    val trackSpotifyId: String,          // Spotify track ID
    val trackName: String,
    val artistName: String,
    val trackUri: String,                // "spotify:track:xxxxx"

    // ── Audio Features (cached from Web API) ───
    val bpm: Float? = null,              // tempo
    val energy: Float? = null,           // 0.0 – 1.0
    val valence: Float? = null,          // 0.0 – 1.0

    // ── Driving Context Snapshot ────────────────
    val speedMph: Float? = null,         // from GPS at playback time
    val weatherCondition: String? = null, // e.g. "clear", "rain", "snow"
    val temperatureCelsius: Float? = null,
    val timeOfDay: String? = null,       // "dawn" | "morning" | "afternoon" | "evening" | "night"

    // ── User Behavior ──────────────────────────
    val wasSkipped: Boolean = false,     // true if user skipped within 30s
    val listenDurationMs: Long = 0,      // how long the user actually listened
    val completionRate: Float = 0f,      // listenDuration / trackDuration (0.0 – 1.0)

    // ── Metadata ───────────────────────────────
    val timestamp: Long = System.currentTimeMillis(),   // epoch millis
    val latitude: Double? = null,        // for future map visualization
    val longitude: Double? = null
)
