package com.example.util

import com.example.localization.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor

enum class MoonPhaseType(val index: Int) {
    NEW_MOON(0),
    WAXING_CRESCENT(1),
    FIRST_QUARTER(2),
    WAXING_GIBBOUS(3),
    FULL_MOON(4),
    WANING_GIBBOUS(5),
    LAST_QUARTER(6),
    WANING_CRESCENT(7)
}

data class LunarStatus(
    val phaseType: MoonPhaseType,
    val phaseName: String,
    val illuminationPercent: Int,
    val lunarAgeDays: Double,
    val cycleFraction: Float, // 0f..1f
    val astrologicalSignificance: String,
    val personalImpact: String
)

data class UpcomingMoonEvent(
    val phaseType: MoonPhaseType,
    val title: String,
    val dateFormatted: String,
    val daysAway: Int,
    val significance: String,
    val impactHint: String
)

object LunarPhaseCalculator {
    private const val SYNODIC_MONTH = 29.530588853
    // Known New Moon: 2000-01-06 18:14 UTC
    private const val REFERENCE_NEW_MOON_MS = 947182440000L

    fun getCurrentLunarStatus(
        zodiacSign: String,
        language: AppLanguage,
        nowMillis: Long = System.currentTimeMillis()
    ): LunarStatus {
        val daysSinceRef = (nowMillis - REFERENCE_NEW_MOON_MS) / 86_400_000.0
        val cycles = daysSinceRef / SYNODIC_MONTH
        val fraction = (cycles - floor(cycles)).toFloat().coerceIn(0f, 0.9999f)
        val ageDays = fraction * SYNODIC_MONTH
        val illumination = ((1.0 - cos(2.0 * PI * fraction)) * 50.0).toInt().coerceIn(0, 100)

        val phaseType = when {
            fraction < 0.03f || fraction >= 0.97f -> MoonPhaseType.NEW_MOON
            fraction < 0.22f -> MoonPhaseType.WAXING_CRESCENT
            fraction < 0.28f -> MoonPhaseType.FIRST_QUARTER
            fraction < 0.47f -> MoonPhaseType.WAXING_GIBBOUS
            fraction < 0.53f -> MoonPhaseType.FULL_MOON
            fraction < 0.72f -> MoonPhaseType.WANING_GIBBOUS
            fraction < 0.78f -> MoonPhaseType.LAST_QUARTER
            else -> MoonPhaseType.WANING_CRESCENT
        }

        val (name, significance, impact) = phaseDetails(phaseType, zodiacSign, language)
        return LunarStatus(
            phaseType = phaseType,
            phaseName = name,
            illuminationPercent = illumination,
            lunarAgeDays = ageDays,
            cycleFraction = fraction,
            astrologicalSignificance = significance,
            personalImpact = impact
        )
    }

    fun getUpcomingEvents(
        zodiacSign: String,
        language: AppLanguage,
        nowMillis: Long = System.currentTimeMillis()
    ): List<UpcomingMoonEvent> {
        val daysSinceRef = (nowMillis - REFERENCE_NEW_MOON_MS) / 86_400_000.0
        val currentCycles = daysSinceRef / SYNODIC_MONTH
        val currentFraction = currentCycles - floor(currentCycles)

        val targets = listOf(
            0.0 to MoonPhaseType.NEW_MOON,
            0.25 to MoonPhaseType.FIRST_QUARTER,
            0.50 to MoonPhaseType.FULL_MOON,
            0.75 to MoonPhaseType.LAST_QUARTER
        )

        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        return targets.map { (targetFraction, type) ->
            var deltaFraction = targetFraction - currentFraction
            if (deltaFraction <= 0.01) {
                deltaFraction += 1.0
            }
            val daysAwayDouble = deltaFraction * SYNODIC_MONTH
            val eventMillis = nowMillis + (daysAwayDouble * 86_400_000.0).toLong()
            val (title, sig, impact) = phaseDetails(type, zodiacSign, language)
            UpcomingMoonEvent(
                phaseType = type,
                title = title,
                dateFormatted = dateFormat.format(Date(eventMillis)),
                daysAway = daysAwayDouble.toInt().coerceAtLeast(1),
                significance = sig,
                impactHint = impact
            )
        }.sortedBy { it.daysAway }
    }

    private fun phaseDetails(
        type: MoonPhaseType,
        zodiacSign: String,
        language: AppLanguage
    ): Triple<String, String, String> {
        val sign = zodiacSign.ifBlank { "your sign" }
        return when (language) {
            AppLanguage.HINDI -> when (type) {
                MoonPhaseType.NEW_MOON -> Triple("अमावस्या (New Moon)", "नई शुरुआत और शांत संकल्प बोने का समय।", "$sign राशि वालों के लिए नए लक्ष्य तय करने का श्रेष्ठ अवसर।")
                MoonPhaseType.WAXING_CRESCENT -> Triple("शुक्ल पक्ष प्रारंभ", "आशा, जिज्ञासा और योजनाओं को आकार देने की ऊर्जा।", "$sign ऊर्जा को रचनात्मक कदमों में बदलें।")
                MoonPhaseType.FIRST_QUARTER -> Triple("शुक्ल अष्टमी (First Quarter)", "निर्णय लेने और बाधाओं को पार करने का समय।", "$sign जातक साहस के साथ आगे बढ़ें।")
                MoonPhaseType.WAXING_GIBBOUS -> Triple("वर्धमान चंद्र", "तैयारी को निखारने और धैर्य रखने का चरण।", "$sign के लिए विवरणों पर ध्यान देने का समय।")
                MoonPhaseType.FULL_MOON -> Triple("पूर्णिमा (Full Moon)", "भावनात्मक स्पष्टता, पूर्णता और प्रकाश का शिखर।", "$sign की अंतर्ज्ञान शक्ति आज प्रबल रहेगी।")
                MoonPhaseType.WANING_GIBBOUS -> Triple("कृष्ण पक्ष प्रारंभ", "कृतज्ञता, ज्ञान साझा करने और समीक्षा का समय।", "$sign जातक अनुभवों से सीख लें।")
                MoonPhaseType.LAST_QUARTER -> Triple("कृष्ण अष्टमी (Last Quarter)", "पुराने बोझ छोड़ने और क्षमा करने का चरण।", "$sign के लिए मानसिक संतुलन बनाने का अवसर।")
                MoonPhaseType.WANING_CRESCENT -> Triple("विश्राम चंद्र", "आत्म-चिंतन, विश्राम और नई ऊर्जा संचय का समय।", "$sign जातक शांति और ध्यान को प्राथमिकता दें।")
            }
            AppLanguage.SPANISH -> when (type) {
                MoonPhaseType.NEW_MOON -> Triple("Luna Nueva", "Momento ideal para sembrar intenciones y nuevos comienzos.", "Impulsa a $sign a definir metas claras desde la calma.")
                MoonPhaseType.WAXING_CRESCENT -> Triple("Luna Creciente", "Energía de impulso, curiosidad y primeros pasos.", "Favorece la creatividad y el enfoque práctico de $sign.")
                MoonPhaseType.FIRST_QUARTER -> Triple("Cuarto Creciente", "Fase de acción decidida y superación de retos.", "Despierta la determinación natural de $sign.")
                MoonPhaseType.WAXING_GIBBOUS -> Triple("Gibosa Creciente", "Tiempo de perfeccionar detalles antes de la culminación.", "Ayuda a $sign a afinar proyectos con paciencia.")
                MoonPhaseType.FULL_MOON -> Triple("Luna Llena", "Plenitud emocional, revelaciones y máxima claridad.", "Amplifica la intuición y sensibilidad de $sign.")
                MoonPhaseType.WANING_GIBBOUS -> Triple("Gibosa Menguante", "Etapa de gratitud, integración y aprendizaje.", "Invita a $sign a compartir sabiduría y agradecer.")
                MoonPhaseType.LAST_QUARTER -> Triple("Cuarto Menguante", "Momento de soltar cargas y cerrar ciclos pendientes.", "Libera espacio mental y emocional para $sign.")
                MoonPhaseType.WANING_CRESCENT -> Triple("Luna Balsámica", "Descanso profundo, introspección y renovación interior.", "Permite a $sign recargar su energía vital.")
            }
            AppLanguage.FRENCH -> when (type) {
                MoonPhaseType.NEW_MOON -> Triple("Nouvelle Lune", "Un portail de renouveau idéal pour poser vos intentions.", "Encourage $sign à initier des projets alignés.")
                MoonPhaseType.WAXING_CRESCENT -> Triple("Premier Croissant", "Élan naissant, curiosité et mise en mouvement.", "Soutient l'inspiration et la clarté de $sign.")
                MoonPhaseType.FIRST_QUARTER -> Triple("Premier Quartier", "Phase d'action, de choix affirmés et de courage.", "Renforce la détermination de $sign.")
                MoonPhaseType.WAXING_GIBBOUS -> Triple("Gibbeuse Croissante", "Temps d'ajustement et de patience constructive.", "Aide $sign à peaufiner ses ambitions.")
                MoonPhaseType.FULL_MOON -> Triple("Pleine Lune", "Apogée émotionnelle, vérité et illumination intérieure.", "Décuple l'intuition et le rayonnement de $sign.")
                MoonPhaseType.WANING_GIBBOUS -> Triple("Gibbeuse Décroissante", "Période de gratitude, de partage et de bilan.", "Invite $sign à transmettre et apprécier le chemin.")
                MoonPhaseType.LAST_QUARTER -> Triple("Dernier Quartier", "Libération des blocages et tri intérieur.", "Aide $sign à tourner la page avec sérénité.")
                MoonPhaseType.WANING_CRESCENT -> Triple("Lune Balsamique", "Repos, introspection douce et régénération.", "Offre à $sign un refuge de calme.")
            }
            else -> when (type) {
                MoonPhaseType.NEW_MOON -> Triple(
                    "New Moon",
                    "A quiet cosmic reset ideal for planting fresh intentions and new beginnings.",
                    "Invites $sign energy to set focused goals before taking outer action."
                )
                MoonPhaseType.WAXING_CRESCENT -> Triple(
                    "Waxing Crescent",
                    "Building momentum, curiosity, and subtle optimism as plans take initial shape.",
                    "Encourages $sign to take small, faithful steps toward emerging desires."
                )
                MoonPhaseType.FIRST_QUARTER -> Triple(
                    "First Quarter Moon",
                    "A decisive turning point calling for courage, clarity, and overcoming friction.",
                    "Activates $sign willpower to adjust course and commit boldly."
                )
                MoonPhaseType.WAXING_GIBBOUS -> Triple(
                    "Waxing Gibbous",
                    "Refinement and dedication as your efforts ripen toward full illumination.",
                    "Supports $sign in polishing details and trusting steady progress."
                )
                MoonPhaseType.FULL_MOON -> Triple(
                    "Full Moon",
                    "Peak illumination bringing heightened emotions, breakthroughs, and culmination.",
                    "Amplifies $sign intuition—celebrate wins and notice what comes to light."
                )
                MoonPhaseType.WANING_GIBBOUS -> Triple(
                    "Waning Gibbous",
                    "Gratitude, reflection, and sharing hard-won wisdom with those around you.",
                    "Helps $sign integrate recent lessons with warmth and perspective."
                )
                MoonPhaseType.LAST_QUARTER -> Triple(
                    "Last Quarter Moon",
                    "Release and recalibration—let go of outdated habits or lingering tension.",
                    "Grants $sign clarity to forgive, declutter, and lighten emotional weight."
                )
                MoonPhaseType.WANING_CRESCENT -> Triple(
                    "Waning Crescent",
                    "Restorative stillness and dreaming before the next lunar cycle begins.",
                    "Urges $sign to prioritize rest, journaling, and gentle self-care."
                )
            }
        }
    }
}
