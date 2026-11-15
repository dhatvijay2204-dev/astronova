package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BirthChartReading
import com.example.model.CompatibilityReading
import com.example.model.DailyAffirmation
import com.example.model.HoroscopeReading
import com.example.model.PlanetaryPlacement
import com.example.model.UserAstrologyProfile
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "local_profile",
    val fullName: String,
    val birthDate: String,
    val birthTime: String,
    val birthPlace: String,
    val zodiacSign: String,
    val vedicRashiName: String,
    val nakshatra: String,
    val nakshatraPada: Int,
    val sunSign: String,
    val isMoonSignManual: Boolean,
    val element: String,
    val focusArea: String,
    val notificationsEnabled: Boolean,
    val notificationHour: Long,
    val notificationMinute: Long,
    val updatedAtMs: Long
) {
    fun toDomain() = UserAstrologyProfile(
        id = id,
        userId = id,
        fullName = fullName,
        birthDate = birthDate,
        birthTime = birthTime,
        birthPlace = birthPlace,
        zodiacSign = zodiacSign,
        vedicRashiName = vedicRashiName,
        nakshatra = nakshatra,
        nakshatraPada = nakshatraPada,
        sunSign = sunSign,
        isMoonSignManual = isMoonSignManual,
        element = element,
        focusArea = focusArea,
        notificationsEnabled = notificationsEnabled,
        notificationHour = notificationHour,
        notificationMinute = notificationMinute,
        updatedAtMs = updatedAtMs
    )

    companion object {
        fun fromDomain(profile: UserAstrologyProfile, nowMs: Long = System.currentTimeMillis()) =
            UserProfileEntity(
                id = "local_profile",
                fullName = profile.fullName.trim().take(60),
                birthDate = profile.birthDate.trim(),
                birthTime = profile.birthTime.trim().ifBlank { "Unknown" }.take(20),
                birthPlace = profile.birthPlace.trim().take(100),
                zodiacSign = profile.zodiacSign.trim(),
                vedicRashiName = profile.vedicRashiName.trim().take(30),
                nakshatra = profile.nakshatra.trim().take(40),
                nakshatraPada = profile.nakshatraPada.coerceIn(1, 4),
                sunSign = profile.sunSign.trim().take(20),
                isMoonSignManual = profile.isMoonSignManual,
                element = profile.element.trim(),
                focusArea = profile.focusArea.trim().take(50),
                notificationsEnabled = profile.notificationsEnabled,
                notificationHour = profile.notificationHour.coerceIn(0L, 23L),
                notificationMinute = profile.notificationMinute.coerceIn(0L, 59L),
                updatedAtMs = nowMs
            )
    }
}

@Entity(tableName = "horoscopes")
data class HoroscopeEntity(
    @PrimaryKey val id: String,
    val periodType: String,
    val periodKey: String,
    val zodiacSign: String,
    val nakshatra: String,
    val headline: String,
    val overview: String,
    val loveInsight: String,
    val careerInsight: String,
    val wellnessInsight: String,
    val luckyNumber: Long,
    val luckyColor: String,
    val powerPlanet: String,
    val cosmicScore: Long,
    val jobPrecautionsJson: String = "[]",
    val lifePrecautionsJson: String = "[]",
    val dashaGocharNote: String = "",
    val updatedAtMs: Long
) {
    fun toDomain() = HoroscopeReading(
        id = id,
        periodType = periodType,
        periodKey = periodKey,
        zodiacSign = zodiacSign,
        nakshatra = nakshatra,
        headline = headline,
        overview = overview,
        loveInsight = loveInsight,
        careerInsight = careerInsight,
        wellnessInsight = wellnessInsight,
        luckyNumber = luckyNumber,
        luckyColor = luckyColor,
        powerPlanet = powerPlanet,
        cosmicScore = cosmicScore,
        jobPrecautions = decodeStringList(jobPrecautionsJson),
        lifePrecautions = decodeStringList(lifePrecautionsJson),
        dashaGocharNote = dashaGocharNote,
        updatedAtMs = updatedAtMs
    )

    companion object {
        fun fromDomain(id: String, reading: HoroscopeReading, nowMs: Long = System.currentTimeMillis()) =
            HoroscopeEntity(
                id = id,
                periodType = reading.periodType,
                periodKey = reading.periodKey,
                zodiacSign = reading.zodiacSign,
                nakshatra = reading.nakshatra.take(60),
                headline = reading.headline.take(200),
                overview = reading.overview.take(3000),
                loveInsight = reading.loveInsight.take(1000),
                careerInsight = reading.careerInsight.take(1000),
                wellnessInsight = reading.wellnessInsight.take(1000),
                luckyNumber = reading.luckyNumber.coerceIn(1L, 99L),
                luckyColor = reading.luckyColor.take(40),
                powerPlanet = reading.powerPlanet.take(40),
                cosmicScore = reading.cosmicScore.coerceIn(1L, 100L),
                jobPrecautionsJson = encodeStringList(reading.jobPrecautions),
                lifePrecautionsJson = encodeStringList(reading.lifePrecautions),
                dashaGocharNote = reading.dashaGocharNote.take(1500),
                updatedAtMs = nowMs
            )

        private fun encodeStringList(items: List<String>): String {
            val arr = JSONArray()
            items.map { it.trim() }.filter { it.isNotBlank() }.take(5).forEach { item ->
                arr.put(item.take(350))
            }
            return arr.toString()
        }

        private fun decodeStringList(raw: String): List<String> {
            return runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length())
                    .map { idx -> arr.optString(idx, "").trim() }
                    .filter { it.isNotBlank() }
            }.getOrDefault(emptyList())
        }
    }
}

@Entity(tableName = "affirmations")
data class AffirmationEntity(
    @PrimaryKey val id: String,
    val dateKey: String,
    val text: String,
    val ritualPrompt: String,
    val zodiacSign: String,
    val focusArea: String,
    val isFavorite: Boolean,
    val updatedAtMs: Long
) {
    fun toDomain() = DailyAffirmation(
        id = id,
        dateKey = dateKey,
        text = text,
        ritualPrompt = ritualPrompt,
        zodiacSign = zodiacSign,
        focusArea = focusArea,
        isFavorite = isFavorite,
        updatedAtMs = updatedAtMs
    )

    companion object {
        fun fromDomain(id: String, aff: DailyAffirmation, nowMs: Long = System.currentTimeMillis()) =
            AffirmationEntity(
                id = id,
                dateKey = aff.dateKey,
                text = aff.text.take(600),
                ritualPrompt = aff.ritualPrompt.take(600),
                zodiacSign = aff.zodiacSign.take(20),
                focusArea = aff.focusArea.take(50),
                isFavorite = aff.isFavorite,
                updatedAtMs = nowMs
            )
    }
}

@Entity(tableName = "birth_charts")
data class BirthChartEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val birthDate: String,
    val birthTime: String,
    val birthPlace: String,
    val sunSign: String,
    val moonSign: String,
    val risingSign: String,
    val nakshatra: String,
    val chartSummary: String,
    val activeDasha: String = "",
    val dashaEffect: String = "",
    val gocharEffect: String = "",
    val placementsJson: String,
    val updatedAtMs: Long
) {
    fun toDomain() = BirthChartReading(
        id = id,
        fullName = fullName,
        birthDate = birthDate,
        birthTime = birthTime,
        birthPlace = birthPlace,
        sunSign = sunSign,
        moonSign = moonSign,
        risingSign = risingSign,
        nakshatra = nakshatra,
        chartSummary = chartSummary,
        activeDasha = activeDasha,
        dashaEffect = dashaEffect,
        gocharEffect = gocharEffect,
        placements = decodePlacements(placementsJson),
        updatedAtMs = updatedAtMs
    )

    companion object {
        fun fromDomain(id: String, chart: BirthChartReading, nowMs: Long = System.currentTimeMillis()) =
            BirthChartEntity(
                id = id,
                fullName = chart.fullName.take(60),
                birthDate = chart.birthDate,
                birthTime = chart.birthTime.take(20),
                birthPlace = chart.birthPlace.take(100),
                sunSign = chart.sunSign.take(20),
                moonSign = chart.moonSign.take(20),
                risingSign = chart.risingSign.take(20),
                nakshatra = chart.nakshatra.take(60),
                chartSummary = chart.chartSummary.take(2500),
                activeDasha = chart.activeDasha.take(200),
                dashaEffect = chart.dashaEffect.take(2000),
                gocharEffect = chart.gocharEffect.take(2000),
                placementsJson = encodePlacements(chart.placements.take(12)),
                updatedAtMs = nowMs
            )

        private fun encodePlacements(placements: List<PlanetaryPlacement>): String {
            val arr = JSONArray()
            placements.forEach { p ->
                arr.put(
                    JSONObject().apply {
                        put("planet", p.planet)
                        put("sign", p.sign)
                        put("degree", p.degree)
                        put("house", p.house)
                        put("meaning", p.meaning)
                    }
                )
            }
            return arr.toString()
        }

        private fun decodePlacements(raw: String): List<PlanetaryPlacement> {
            return runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length()).map { idx ->
                    val obj = arr.getJSONObject(idx)
                    PlanetaryPlacement(
                        planet = obj.optString("planet", ""),
                        sign = obj.optString("sign", "Aries"),
                        degree = obj.optLong("degree", 0L),
                        house = obj.optLong("house", 1L),
                        meaning = obj.optString("meaning", "")
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}

@Entity(tableName = "compatibilities")
data class CompatibilityEntity(
    @PrimaryKey val id: String,
    val signOne: String,
    val signTwo: String,
    val relationshipType: String,
    val overallScore: Long,
    val romanticScore: Long,
    val platonicScore: Long,
    val professionalScore: Long,
    val summary: String,
    val strengths: String,
    val challenges: String,
    val updatedAtMs: Long
) {
    fun toDomain() = CompatibilityReading(
        id = id,
        signOne = signOne,
        signTwo = signTwo,
        relationshipType = relationshipType,
        overallScore = overallScore,
        romanticScore = romanticScore,
        platonicScore = platonicScore,
        professionalScore = professionalScore,
        summary = summary,
        strengths = strengths,
        challenges = challenges,
        updatedAtMs = updatedAtMs
    )

    companion object {
        fun fromDomain(id: String, reading: CompatibilityReading, nowMs: Long = System.currentTimeMillis()) =
            CompatibilityEntity(
                id = id,
                signOne = reading.signOne.take(20),
                signTwo = reading.signTwo.take(20),
                relationshipType = reading.relationshipType,
                overallScore = reading.overallScore.coerceIn(1L, 100L),
                romanticScore = reading.romanticScore.coerceIn(1L, 100L),
                platonicScore = reading.platonicScore.coerceIn(1L, 100L),
                professionalScore = reading.professionalScore.coerceIn(1L, 100L),
                summary = reading.summary.take(2000),
                strengths = reading.strengths.take(1000),
                challenges = reading.challenges.take(1000),
                updatedAtMs = nowMs
            )
    }
}
