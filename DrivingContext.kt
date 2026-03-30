package com.yeoui.domain.model

/**
 * DrivingContext
 *
 * Domain model representing the driver's current environmental state.
 * The Context Analyzer aggregates sensor data into this object,
 * which the Audio Feature Mapper then converts into target Spotify
 * audio feature ranges (BPM, energy, valence).
 *
 * Flow: Sensors → DrivingContext → AudioFeatureTarget → Spotify Recommendations
 */
data class DrivingContext(
    val speedMph: Float,
    val weather: WeatherState,
    val timeOfDay: TimeOfDay,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Driving intensity derived from speed.
     * Used as a primary input for BPM and energy mapping.
     */
    val intensity: DrivingIntensity
        get() = when {
            speedMph < 15f  -> DrivingIntensity.STATIONARY
            speedMph < 35f  -> DrivingIntensity.CITY
            speedMph < 60f  -> DrivingIntensity.SUBURBAN
            speedMph < 80f  -> DrivingIntensity.HIGHWAY
            else            -> DrivingIntensity.FAST
        }
}

enum class DrivingIntensity(val targetBpmRange: IntRange, val targetEnergyRange: ClosedFloatingPointRange<Float>) {
    STATIONARY(60..90,    0.1f..0.3f),
    CITY(      85..110,   0.3f..0.5f),
    SUBURBAN(  100..125,  0.4f..0.7f),
    HIGHWAY(   115..140,  0.6f..0.85f),
    FAST(      130..170,  0.75f..1.0f)
}

enum class WeatherState(val valenceModifier: Float) {
    CLEAR(0.1f),
    CLOUDY(0.0f),
    RAIN(-0.15f),
    STORM(-0.25f),
    SNOW(-0.1f),
    FOG(-0.05f)
}

enum class TimeOfDay {
    DAWN,       // 5:00 – 7:59
    MORNING,    // 8:00 – 11:59
    AFTERNOON,  // 12:00 – 16:59
    EVENING,    // 17:00 – 20:59
    NIGHT       // 21:00 – 4:59
}
