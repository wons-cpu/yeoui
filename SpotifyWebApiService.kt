package com.yeoui.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * SpotifyWebApiService
 *
 * Retrofit interface for Spotify Web API endpoints.
 * Used to fetch audio features (BPM, energy, valence) that the
 * Context Engine maps to driving conditions.
 *
 * Auth: All requests require a Bearer token via Spotify OAuth 2.0 (PKCE flow).
 *
 * @see <a href="https://developer.spotify.com/documentation/web-api">Spotify Web API Docs</a>
 */
interface SpotifyWebApiService {

    companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"
    }

    // ─────────────────────────────────────────────
    // Audio Features — Core of Context ↔ Music Mapping
    // ─────────────────────────────────────────────

    /**
     * Get audio features for a single track.
     * Key fields used by Yeoui's Context Engine:
     *   - tempo (BPM): mapped to driving speed
     *   - energy (0.0–1.0): mapped to driving intensity
     *   - valence (0.0–1.0): mapped to weather/mood context
     */
    @GET("audio-features/{id}")
    suspend fun getAudioFeatures(
        @Header("Authorization") bearerToken: String,
        @Path("id") trackId: String
    ): AudioFeaturesResponse

    /**
     * Get audio features for multiple tracks (batch, max 100).
     * Used when pre-analyzing a user's saved library or playlist.
     */
    @GET("audio-features")
    suspend fun getMultipleAudioFeatures(
        @Header("Authorization") bearerToken: String,
        @Query("ids") trackIds: String // comma-separated
    ): MultipleAudioFeaturesResponse

    // ─────────────────────────────────────────────
    // Search & Recommendations
    // ─────────────────────────────────────────────

    /**
     * Get Spotify's seed-based recommendations.
     * The Context Engine provides target audio feature ranges
     * derived from the current driving context.
     */
    @GET("recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") bearerToken: String,
        @Query("seed_genres") seedGenres: String? = null,
        @Query("seed_tracks") seedTracks: String? = null,
        @Query("target_tempo") targetBpm: Float? = null,
        @Query("min_energy") minEnergy: Float? = null,
        @Query("max_energy") maxEnergy: Float? = null,
        @Query("target_valence") targetValence: Float? = null,
        @Query("limit") limit: Int = 20
    ): RecommendationsResponse

    /**
     * Get the current user's profile.
     * Used for personalization and Music Map ownership.
     */
    @GET("me")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") bearerToken: String
    ): UserProfileResponse
}

// ─────────────────────────────────────────────
// Response Data Classes
// ─────────────────────────────────────────────

/**
 * Audio features for a single track.
 * @see <a href="https://developer.spotify.com/documentation/web-api/reference/get-audio-features">API Reference</a>
 */
data class AudioFeaturesResponse(
    val id: String,
    val tempo: Float,           // BPM (e.g., 120.0)
    val energy: Float,          // 0.0 (calm) → 1.0 (intense)
    val valence: Float,         // 0.0 (sad/dark) → 1.0 (happy/bright)
    val danceability: Float,    // 0.0 → 1.0
    val acousticness: Float,    // 0.0 → 1.0
    val instrumentalness: Float // 0.0 (vocal) → 1.0 (instrumental)
)

data class MultipleAudioFeaturesResponse(
    val audio_features: List<AudioFeaturesResponse?>
)

data class RecommendationsResponse(
    val tracks: List<TrackObject>
)

data class TrackObject(
    val id: String,
    val name: String,
    val uri: String,             // "spotify:track:xxxxx"
    val artists: List<ArtistObject>,
    val duration_ms: Long
)

data class ArtistObject(
    val id: String,
    val name: String
)

data class UserProfileResponse(
    val id: String,
    val display_name: String?,
    val country: String?
)
