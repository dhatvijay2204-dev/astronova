package com.example.data.ai

import android.util.Log
import com.example.localization.AppLanguage
import com.example.model.BirthChartReading
import com.example.model.CompatibilityReading
import com.example.model.DailyAffirmation
import com.example.model.HoroscopeReading
import com.example.model.PlanetaryPlacement
import com.example.model.UserAstrologyProfile
import com.example.util.ZodiacCalculator
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class AiAstrologyService {

    private val horoscopeSchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(description = "A poetic, inspiring 4-8 word horoscope title"),
            "overview" to Schema.string(description = "Detailed 3-4 sentence personalized cosmic forecast"),
            "loveInsight" to Schema.string(description = "2 sentence romantic and interpersonal insight"),
            "careerInsight" to Schema.string(description = "2 sentence career, finance, and purpose insight"),
            "wellnessInsight" to Schema.string(description = "2 sentence emotional and physical wellness insight"),
            "luckyNumber" to Schema.integer(description = "Lucky integer between 1 and 99"),
            "luckyColor" to Schema.string(description = "Lucky color name"),
            "powerPlanet" to Schema.string(description = "Dominant guiding planet"),
            "cosmicScore" to Schema.integer(description = "Cosmic alignment score between 60 and 99"),
            "jobPrecautions" to Schema.array(
                items = Schema.string(description = "One specific, actionable job/career/workplace precaution to take"),
                description = "2 to 3 major precautions to take in job, workplace, colleagues, contracts, or business according to Rashibhavishya"
            ),
            "lifePrecautions" to Schema.array(
                items = Schema.string(description = "One specific, actionable personal life, family, health, speech, or travel precaution to take"),
                description = "2 to 3 major precautions to take in personal life, relationships, finances, health, or daily choices according to Rashibhavishya"
            ),
            "dashaGocharNote" to Schema.string(description = "2 concise sentences explaining how the active Vimshottari Mahadasha/Antardasha and today's Gochar transits shape this Rashifal")
        )
    )

    private val affirmationSchema = Schema.obj(
        properties = mapOf(
            "text" to Schema.string(description = "A deeply personal first-person daily affirmation (1-2 sentences)"),
            "ritualPrompt" to Schema.string(description = "A calming 1-2 sentence mindful action or reflection prompt")
        )
    )

    private val birthChartSchema = Schema.obj(
        properties = mapOf(
            "risingSign" to Schema.string(description = "Calculated or estimated Vedic Lagna / Ascendant zodiac sign name in English (e.g. Leo, Libra)"),
            "chartSummary" to Schema.string(description = "3-4 sentence holistic Vedic Kundali synthesis prioritizing Chandra Rashi (Moon Sign), Janma Nakshatra, Surya (Sun), and Lagna"),
            "dashaEffect" to Schema.string(description = "3 sentences analyzing the current Vimshottari Mahadasha and Antardasha lords and how they activate specific Bhavas/planets in this personal Kundali"),
            "gocharEffect" to Schema.string(description = "3 sentences analyzing current Gochar transits (Jupiter, Saturn, Rahu/Ketu, Sun, Moon) from this Kundali's Moon Sign and Lagna, noting opportunities or cautions"),
            "placements" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "planet" to Schema.string(description = "Vedic Navagraha or Lagna name: Moon, Sun, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu, or Ascendant"),
                        "sign" to Schema.string(description = "Sidereal zodiac sign name in English (Aries through Pisces)"),
                        "degree" to Schema.integer(description = "Sidereal degree within sign from 0 to 29"),
                        "house" to Schema.integer(description = "Vedic Bhava / house number from 1 to 12"),
                        "meaning" to Schema.string(description = "2 sentence personalized Vedic Jyotish explanation of this Navagraha placement")
                    )
                ),
                description = "List of 10 Vedic Kundali placements (Moon, Sun, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu, Ascendant)"
            )
        )
    )

    private val compatibilitySchema = Schema.obj(
        properties = mapOf(
            "overallScore" to Schema.integer(description = "Overall Vedic compatibility score reflecting Chandra Rashi & Guna Milan harmony for the selected relationship type (1 to 100)"),
            "romanticScore" to Schema.integer(description = "Romantic & emotional Chandra chemistry score (1 to 100)"),
            "platonicScore" to Schema.integer(description = "Platonic Maitri & friendship score (1 to 100)"),
            "professionalScore" to Schema.integer(description = "Professional collaboration & Karma alignment score (1 to 100)"),
            "summary" to Schema.string(description = "3 sentence tailored Vedic Moon-Sign compatibility explanation referencing Rashi lords and Ashtakoota harmony"),
            "strengths" to Schema.string(description = "2 sentence summary of shared Vedic strengths and supportive planetary lords"),
            "challenges" to Schema.string(description = "2 sentence summary of growth areas and practical remedies or balance")
        )
    )

    private val horoscopeModel by lazy { buildJsonModel(MODEL_NAME, horoscopeSchema) }
    private val horoscopeFastModel by lazy { buildJsonModel(FAST_MODEL_NAME, horoscopeSchema) }

    private val affirmationModel by lazy { buildJsonModel(MODEL_NAME, affirmationSchema) }
    private val affirmationFastModel by lazy { buildJsonModel(FAST_MODEL_NAME, affirmationSchema) }

    private val birthChartModel by lazy { buildJsonModel(MODEL_NAME, birthChartSchema) }
    private val birthChartFastModel by lazy { buildJsonModel(FAST_MODEL_NAME, birthChartSchema) }

    private val compatibilityModel by lazy { buildJsonModel(MODEL_NAME, compatibilitySchema) }
    private val compatibilityFastModel by lazy { buildJsonModel(FAST_MODEL_NAME, compatibilitySchema) }

    private fun buildJsonModel(modelName: String, schema: Schema): GenerativeModel {
        return Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private suspend fun generateJsonWithFallback(
        primaryModel: GenerativeModel,
        fallbackModel: GenerativeModel,
        prompt: String,
        operationName: String
    ): JSONObject {
        val rawText = try {
            collectStreamWithinTimeout(primaryModel, prompt, PRIMARY_TIMEOUT_MS)
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) throw e
            Log.w(TAG, "$operationName timed out on $MODEL_NAME stream; falling back to $FAST_MODEL_NAME stream", e)
            collectFastWithUnaryBackup(fallbackModel, prompt, operationName)
        } catch (e: Exception) {
            Log.w(
                TAG,
                "$operationName failed on $MODEL_NAME stream (${e.javaClass.simpleName}: ${e.cause?.message ?: e.message}); falling back to $FAST_MODEL_NAME",
                e
            )
            collectFastWithUnaryBackup(fallbackModel, prompt, operationName)
        }
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            throw IllegalStateException("Empty AI response for $operationName.")
        }
        return JSONObject(trimmed)
    }

    private suspend fun collectFastWithUnaryBackup(
        fallbackModel: GenerativeModel,
        prompt: String,
        operationName: String
    ): String {
        return try {
            collectStreamWithinTimeout(fallbackModel, prompt, FALLBACK_TIMEOUT_MS)
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) throw e
            Log.w(TAG, "$operationName timed out on $FAST_MODEL_NAME stream; retrying unary generateContent", e)
            generateUnaryWithinTimeout(fallbackModel, prompt, UNARY_TIMEOUT_MS)
        } catch (e: Exception) {
            Log.w(
                TAG,
                "$operationName failed on $FAST_MODEL_NAME stream (${e.javaClass.simpleName}: ${e.cause?.message ?: e.message}); retrying unary generateContent",
                e
            )
            generateUnaryWithinTimeout(fallbackModel, prompt, UNARY_TIMEOUT_MS)
        }
    }

    private suspend fun collectStreamWithinTimeout(
        model: GenerativeModel,
        prompt: String,
        timeoutMs: Long
    ): String = withContext(Dispatchers.IO) {
        withTimeout(timeoutMs) {
            val sb = StringBuilder()
            model.generateContentStream(prompt).collect { chunk ->
                val part = chunk.text
                if (!part.isNullOrEmpty()) {
                    sb.append(part)
                }
            }
            val result = sb.toString()
            if (result.isBlank()) {
                throw IllegalStateException("Empty streaming response from model.")
            }
            result
        }
    }

    private suspend fun generateUnaryWithinTimeout(
        model: GenerativeModel,
        prompt: String,
        timeoutMs: Long
    ): String = withContext(Dispatchers.IO) {
        withTimeout(timeoutMs) {
            val response = model.generateContent(prompt)
            response.text ?: throw IllegalStateException("Empty unary response from model.")
        }
    }

    suspend fun generateHoroscope(
        profile: UserAstrologyProfile,
        periodType: String,
        periodKey: String,
        language: AppLanguage
    ): HoroscopeReading {
        val periodLabel = if (periodType == "monthly") {
            "monthly Vedic Rashifal outlook for $periodKey"
        } else {
            "daily Vedic Rashifal for $periodKey"
        }
        val rashiLabel = if (profile.vedicRashiName.isNotBlank()) {
            "${profile.vedicRashiName} (${profile.zodiacSign})"
        } else {
            profile.zodiacSign
        }
        val nakshatraLabel = if (profile.nakshatra.isNotBlank()) {
            "${profile.nakshatra} (Pada ${profile.nakshatraPada})"
        } else {
            "Janma Nakshatra"
        }
        val dg = ZodiacCalculator.calculateDashaAndGochar(
            birthDate = profile.birthDate,
            birthTime = profile.birthTime,
            natalMoonSign = profile.zodiacSign
        )
        val dashaSummary = "${dg.dasha.mahadashaLord} Mahadasha (${dg.dasha.mahadashaStart}–${dg.dasha.mahadashaEnd}) / ${dg.dasha.antardashaLord} Antardasha (${dg.dasha.antardashaStart}–${dg.dasha.antardashaEnd})"
        val gocharLines = dg.transits.take(6).joinToString("; ") { t ->
            val note = if (t.specialNote.isNotBlank()) " [${t.specialNote}]" else ""
            "${t.planet} in ${t.transitSign.sanskritName} (${t.houseFromMoon}H from Moon, ${t.favorability}$note)"
        }
        val prompt = """
            You are an empathetic master of Vedic Astrology (Jyotish Shastra / Rashibhavishya).
            Base this reading strictly on Sidereal Vedic MOON SIGN (Chandra Rashi), active Vimshottari Dasha, and Gochar transits relative to the Moon Sign (NOT Western Sun sign).

            Generate a concise, personalized $periodLabel for:
            - Name: ${profile.fullName}
            - Moon Sign (Chandra Rashi): $rashiLabel (${profile.element})
            - Janma Nakshatra: $nakshatraLabel
            - Active Vimshottari Dasha: $dashaSummary
            - Current Gochar from Moon: $gocharLines
            - Sidereal Sun Sign: ${profile.sunSign.ifBlank { "Sidereal" }}
            - Birth: ${profile.birthDate} ${profile.birthTime}, ${profile.birthPlace}
            - Focus: ${profile.focusArea}

            Include 2-3 specific "jobPrecautions" and 2-3 "lifePrecautions" tailored to this Dasha and Gochar, plus "dashaGocharNote" explaining how the Dasha & Gochar drive today's accuracy.
            Write all text fields idiomatically in ${language.promptName}.
        """.trimIndent()

        val json = generateJsonWithFallback(
            primaryModel = horoscopeModel,
            fallbackModel = horoscopeFastModel,
            prompt = prompt,
            operationName = "horoscope"
        )
        return HoroscopeReading(
            userId = profile.userId,
            periodType = periodType,
            periodKey = periodKey,
            zodiacSign = profile.zodiacSign,
            nakshatra = if (profile.nakshatra.isNotBlank()) "${profile.nakshatra} • Pada ${profile.nakshatraPada}" else "",
            headline = json.getString("headline"),
            overview = json.getString("overview"),
            loveInsight = json.getString("loveInsight"),
            careerInsight = json.getString("careerInsight"),
            wellnessInsight = json.getString("wellnessInsight"),
            luckyNumber = json.optInt("luckyNumber", 7).toLong().coerceIn(1L, 99L),
            luckyColor = json.getString("luckyColor"),
            powerPlanet = json.getString("powerPlanet"),
            cosmicScore = json.optInt("cosmicScore", 84).toLong().coerceIn(1L, 100L),
            jobPrecautions = parseStringArray(json, "jobPrecautions"),
            lifePrecautions = parseStringArray(json, "lifePrecautions"),
            dashaGocharNote = json.optString("dashaGocharNote", "")
        )
    }

    private fun parseStringArray(json: JSONObject, key: String): List<String> {
        val arr = json.optJSONArray(key) ?: return emptyList()
        return (0 until arr.length())
            .map { idx -> arr.optString(idx, "").trim() }
            .filter { it.isNotBlank() }
    }

    suspend fun generateAffirmation(
        profile: UserAstrologyProfile,
        intention: String,
        dateKey: String,
        language: AppLanguage
    ): DailyAffirmation {
        val rashiLabel = if (profile.vedicRashiName.isNotBlank()) {
            "${profile.vedicRashiName} (${profile.zodiacSign})"
        } else {
            profile.zodiacSign
        }
        val prompt = """
            Craft an uplifting Vedic daily affirmation (Sankalpa) and brief mindful ritual aligned with:
            - Name: ${profile.fullName}
            - Vedic Moon Sign (Chandra Rashi): $rashiLabel (${profile.element})
            - Janma Nakshatra: ${profile.nakshatra.ifBlank { "Lunar Asterism" }} (Pada ${profile.nakshatraPada})
            - Intention: $intention
            - Date: $dateKey

            Write both "text" and "ritualPrompt" in ${language.promptName}, concise enough for a push notification.
        """.trimIndent()

        val json = generateJsonWithFallback(
            primaryModel = affirmationModel,
            fallbackModel = affirmationFastModel,
            prompt = prompt,
            operationName = "affirmation"
        )
        return DailyAffirmation(
            userId = profile.userId,
            dateKey = dateKey,
            text = json.getString("text"),
            ritualPrompt = json.getString("ritualPrompt"),
            zodiacSign = profile.zodiacSign,
            focusArea = intention,
            isFavorite = false
        )
    }

    suspend fun generateBirthChart(
        userId: String,
        fullName: String,
        birthDate: String,
        birthTime: String,
        birthPlace: String,
        moonSign: String,
        sunSign: String,
        nakshatra: String,
        language: AppLanguage
    ): BirthChartReading {
        val dg = ZodiacCalculator.calculateDashaAndGochar(
            birthDate = birthDate,
            birthTime = birthTime,
            natalMoonSign = moonSign
        )
        val activeDashaLabel = "${dg.dasha.mahadashaLord} Mahadasha • ${dg.dasha.antardashaLord} Antardasha (${dg.dasha.antardashaStart} – ${dg.dasha.antardashaEnd})"
        val gocharSummaryPrompt = dg.transits.joinToString("; ") { t ->
            val note = if (t.specialNote.isNotBlank()) " [${t.specialNote}]" else ""
            "${t.planet} in ${t.transitSign.sanskritName} (${t.houseFromMoon}H from Moon, ${t.favorability}$note)"
        }
        val prompt = """
            Compute and interpret a Sidereal (Lahiri Ayanamsa) Vedic Birth Chart (Janma Kundali) with Dasha & Gochar impact for:
            - Name: $fullName
            - Sidereal Moon Sign (Chandra Rashi): $moonSign
            - Janma Nakshatra: $nakshatra
            - Sidereal Sun Sign (Surya Rashi): $sunSign
            - Birth: $birthDate $birthTime, $birthPlace
            - Current Vimshottari Dasha: $activeDashaLabel (Mahadasha ${dg.dasha.mahadashaStart}–${dg.dasha.mahadashaEnd})
            - Current Gochar Transits from Janma Rashi: $gocharSummaryPrompt

            Calculate Lagna (Rising Sign) and 10 Vedic placements: Moon, Sun, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu, Ascendant.
            Keep "risingSign" and each placement's "planet" and "sign" in standard English names (Aries..Pisces). Ensure Moon matches $moonSign and Sun matches $sunSign.
            Explain "dashaEffect" (how active Mahadasha/Antardasha lords affect this Kundali's houses and planets) and "gocharEffect" (how current Jupiter, Saturn, Rahu/Ketu, Sun, Moon transits impact this personal Kundali right now).
            Write "chartSummary", "dashaEffect", "gocharEffect", and each placement "meaning" clearly in ${language.promptName}.
        """.trimIndent()

        val json = generateJsonWithFallback(
            primaryModel = birthChartModel,
            fallbackModel = birthChartFastModel,
            prompt = prompt,
            operationName = "birth chart"
        )
        val placementsArray = json.getJSONArray("placements")
        val placements = (0 until placementsArray.length()).map { idx ->
            val obj = placementsArray.getJSONObject(idx)
            PlanetaryPlacement(
                planet = obj.getString("planet"),
                sign = obj.getString("sign"),
                degree = obj.optInt("degree", 12).toLong().coerceIn(0L, 29L),
                house = obj.optInt("house", (idx % 12) + 1).toLong().coerceIn(1L, 12L),
                meaning = obj.getString("meaning")
            )
        }

        return BirthChartReading(
            userId = userId,
            fullName = fullName,
            birthDate = birthDate,
            birthTime = birthTime,
            birthPlace = birthPlace,
            sunSign = sunSign,
            moonSign = moonSign,
            risingSign = json.getString("risingSign"),
            nakshatra = nakshatra,
            chartSummary = json.getString("chartSummary"),
            activeDasha = activeDashaLabel,
            dashaEffect = json.optString("dashaEffect", ""),
            gocharEffect = json.optString("gocharEffect", ""),
            placements = placements
        )
    }

    suspend fun generateCompatibility(
        userId: String,
        signOne: String,
        signTwo: String,
        relationshipType: String,
        language: AppLanguage
    ): CompatibilityReading {
        val prompt = """
            Analyze Vedic Astrology (Jyotish) Moon-Sign compatibility (Chandra Rashi & Ashtakoota Guna Milan) between Moon Sign $signOne and Moon Sign $signTwo for a $relationshipType relationship.
            Evaluate Rashi lords (Graha Maitri) and Bhakoot balance.
            Provide scores (1-100) for overallScore, romanticScore, platonicScore, professionalScore.
            Write "summary", "strengths", and "challenges" in ${language.promptName}.
        """.trimIndent()

        val json = generateJsonWithFallback(
            primaryModel = compatibilityModel,
            fallbackModel = compatibilityFastModel,
            prompt = prompt,
            operationName = "compatibility"
        )
        return CompatibilityReading(
            userId = userId,
            signOne = signOne,
            signTwo = signTwo,
            relationshipType = relationshipType,
            overallScore = json.optInt("overallScore", 80).toLong().coerceIn(1L, 100L),
            romanticScore = json.optInt("romanticScore", 80).toLong().coerceIn(1L, 100L),
            platonicScore = json.optInt("platonicScore", 80).toLong().coerceIn(1L, 100L),
            professionalScore = json.optInt("professionalScore", 80).toLong().coerceIn(1L, 100L),
            summary = json.getString("summary"),
            strengths = json.getString("strengths"),
            challenges = json.getString("challenges")
        )
    }

    suspend fun generatePersonCompatibility(
        userId: String,
        personOneName: String,
        personOneBirthDate: String,
        personOneBirthTime: String,
        personOneBirthPlace: String,
        personOneMoonSign: String,
        personOneNakshatra: String,
        personTwoName: String,
        personTwoBirthDate: String,
        personTwoBirthTime: String,
        personTwoBirthPlace: String,
        personTwoMoonSign: String,
        personTwoNakshatra: String,
        relationshipType: String,
        language: AppLanguage
    ): CompatibilityReading {
        val prompt = """
            Generate a personalized Vedic Ashtakoota / Moon-Sign (Chandra Rashi) compatibility report:
            Person 1: $personOneName — Moon Sign: $personOneMoonSign, Nakshatra: $personOneNakshatra, born $personOneBirthDate $personOneBirthTime, $personOneBirthPlace.
            Person 2: $personTwoName — Moon Sign: $personTwoMoonSign, Nakshatra: $personTwoNakshatra, born $personTwoBirthDate $personTwoBirthTime, $personTwoBirthPlace.
            Focus: $relationshipType.

            Provide scores (1-100) for overallScore, romanticScore, platonicScore, professionalScore.
            Write "summary", "strengths", and "challenges" referencing their Moon Signs & Nakshatras in ${language.promptName}.
        """.trimIndent()

        val json = generateJsonWithFallback(
            primaryModel = compatibilityModel,
            fallbackModel = compatibilityFastModel,
            prompt = prompt,
            operationName = "person compatibility"
        )
        return CompatibilityReading(
            userId = userId,
            signOne = personOneMoonSign,
            signTwo = personTwoMoonSign,
            relationshipType = relationshipType,
            overallScore = json.optInt("overallScore", 82).toLong().coerceIn(1L, 100L),
            romanticScore = json.optInt("romanticScore", 82).toLong().coerceIn(1L, 100L),
            platonicScore = json.optInt("platonicScore", 82).toLong().coerceIn(1L, 100L),
            professionalScore = json.optInt("professionalScore", 82).toLong().coerceIn(1L, 100L),
            summary = json.getString("summary"),
            strengths = json.getString("strengths"),
            challenges = json.getString("challenges")
        )
    }

    private companion object {
        const val TAG = "AiAstrologyService"
        const val MODEL_NAME = "gemini-3.8-flash"
        const val FAST_MODEL_NAME = "gemini-3.5-flash-lite"
        const val PRIMARY_TIMEOUT_MS = 25_000L
        const val FALLBACK_TIMEOUT_MS = 25_000L
        const val UNARY_TIMEOUT_MS = 30_000L
    }
}
