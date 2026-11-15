package com.example.model

data class UserAstrologyProfile(
    val id: String = "local_profile",
    val userId: String = "local_user",
    val fullName: String = "",
    val birthDate: String = "",
    val birthTime: String = "Unknown",
    val birthPlace: String = "",
    val zodiacSign: String = "", // Primary Vedic Moon Sign (Chandra Rashi)
    val vedicRashiName: String = "",
    val nakshatra: String = "",
    val nakshatraPada: Int = 1,
    val sunSign: String = "", // Sidereal Surya Rashi
    val isMoonSignManual: Boolean = false,
    val element: String = "Fire",
    val focusArea: String = "Balanced",
    val notificationsEnabled: Boolean = true,
    val notificationHour: Long = 8L,
    val notificationMinute: Long = 0L,
    val updatedAtMs: Long = System.currentTimeMillis()
)

data class HoroscopeReading(
    val id: String = "",
    val userId: String = "local_user",
    val periodType: String = "daily",
    val periodKey: String = "",
    val zodiacSign: String = "", // Moon Sign (Rashi)
    val nakshatra: String = "",
    val headline: String = "",
    val overview: String = "",
    val loveInsight: String = "",
    val careerInsight: String = "",
    val wellnessInsight: String = "",
    val luckyNumber: Long = 7L,
    val luckyColor: String = "",
    val powerPlanet: String = "",
    val cosmicScore: Long = 85L,
    val jobPrecautions: List<String> = emptyList(),
    val lifePrecautions: List<String> = emptyList(),
    val dashaGocharNote: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
)

data class DailyAffirmation(
    val id: String = "",
    val userId: String = "local_user",
    val dateKey: String = "",
    val text: String = "",
    val ritualPrompt: String = "",
    val zodiacSign: String = "",
    val focusArea: String = "Balanced",
    val isFavorite: Boolean = false,
    val updatedAtMs: Long = System.currentTimeMillis()
)

data class PlanetaryPlacement(
    val planet: String = "",
    val sign: String = "",
    val degree: Long = 0L,
    val house: Long = 1L,
    val meaning: String = ""
)

data class BirthChartReading(
    val id: String = "",
    val userId: String = "local_user",
    val fullName: String = "",
    val birthDate: String = "",
    val birthTime: String = "",
    val birthPlace: String = "",
    val sunSign: String = "",
    val moonSign: String = "",
    val risingSign: String = "",
    val nakshatra: String = "",
    val chartSummary: String = "",
    val activeDasha: String = "",
    val dashaEffect: String = "",
    val gocharEffect: String = "",
    val placements: List<PlanetaryPlacement> = emptyList(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

data class CompatibilityReading(
    val id: String = "",
    val userId: String = "local_user",
    val signOne: String = "",
    val signTwo: String = "",
    val relationshipType: String = "Romantic",
    val overallScore: Long = 80L,
    val romanticScore: Long = 80L,
    val platonicScore: Long = 80L,
    val professionalScore: Long = 80L,
    val summary: String = "",
    val strengths: String = "",
    val challenges: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
)

data class ZodiacSignInfo(
    val name: String,
    val sanskritName: String,
    val symbol: String,
    val dateRange: String,
    val element: String,
    val modality: String,
    val rulingPlanet: String
)

data class VedicMoonDetails(
    val moonSign: ZodiacSignInfo,
    val sunSign: ZodiacSignInfo,
    val nakshatra: String,
    val nakshatraLord: String,
    val pada: Int,
    val moonDegreeInSign: Double
)

data class DashaSnapshot(
    val mahadashaLord: String,
    val mahadashaStart: String,
    val mahadashaEnd: String,
    val antardashaLord: String,
    val antardashaStart: String,
    val antardashaEnd: String,
    val birthNakshatra: String,
    val birthNakshatraLord: String,
    val balanceYearsAtBirth: Double
)

data class GocharTransit(
    val planet: String,
    val transitSign: ZodiacSignInfo,
    val houseFromMoon: Int,
    val favorability: String,
    val specialNote: String = ""
)

data class DashaGocharContext(
    val dasha: DashaSnapshot,
    val transits: List<GocharTransit>,
    val isSadeSati: Boolean,
    val isChandrashtama: Boolean
)

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
