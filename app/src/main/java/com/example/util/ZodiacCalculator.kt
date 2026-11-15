package com.example.util

import com.example.model.DashaGocharContext
import com.example.model.DashaSnapshot
import com.example.model.GocharTransit
import com.example.model.VedicMoonDetails
import com.example.model.ZodiacSignInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

object ZodiacCalculator {

    val allSigns: List<ZodiacSignInfo> = listOf(
        ZodiacSignInfo("Aries", "Mesha", "♈", "Mesha • Fire", "Fire", "Cardinal", "Mars (Mangala)"),
        ZodiacSignInfo("Taurus", "Vrishabha", "♉", "Vrishabha • Earth", "Earth", "Fixed", "Venus (Shukra)"),
        ZodiacSignInfo("Gemini", "Mithuna", "♊", "Mithuna • Air", "Air", "Mutable", "Mercury (Budha)"),
        ZodiacSignInfo("Cancer", "Karka", "♋", "Karka • Water", "Water", "Cardinal", "Moon (Chandra)"),
        ZodiacSignInfo("Leo", "Simha", "♌", "Simha • Fire", "Fire", "Fixed", "Sun (Surya)"),
        ZodiacSignInfo("Virgo", "Kanya", "♍", "Kanya • Earth", "Earth", "Mutable", "Mercury (Budha)"),
        ZodiacSignInfo("Libra", "Tula", "♎", "Tula • Air", "Air", "Cardinal", "Venus (Shukra)"),
        ZodiacSignInfo("Scorpio", "Vrishchika", "♏", "Vrishchika • Water", "Water", "Fixed", "Mars (Mangala)"),
        ZodiacSignInfo("Sagittarius", "Dhanu", "♐", "Dhanu • Fire", "Fire", "Mutable", "Jupiter (Guru)"),
        ZodiacSignInfo("Capricorn", "Makara", "♑", "Makara • Earth", "Earth", "Cardinal", "Saturn (Shani)"),
        ZodiacSignInfo("Aquarius", "Kumbha", "♒", "Kumbha • Air", "Air", "Fixed", "Saturn (Shani)"),
        ZodiacSignInfo("Pisces", "Meena", "♓", "Meena • Water", "Water", "Mutable", "Jupiter (Guru)")
    )

    // 27 Vedic Nakshatras with their Vimshottari Dasha Lords
    val nakshatras: List<Pair<String, String>> = listOf(
        "Ashwini" to "Ketu",
        "Bharani" to "Venus",
        "Krittika" to "Sun",
        "Rohini" to "Moon",
        "Mrigashira" to "Mars",
        "Ardra" to "Rahu",
        "Punarvasu" to "Jupiter",
        "Pushya" to "Saturn",
        "Ashlesha" to "Mercury",
        "Magha" to "Ketu",
        "Purva Phalguni" to "Venus",
        "Uttara Phalguni" to "Sun",
        "Hasta" to "Moon",
        "Chitra" to "Mars",
        "Swati" to "Rahu",
        "Vishakha" to "Jupiter",
        "Anuradha" to "Saturn",
        "Jyeshtha" to "Mercury",
        "Mula" to "Ketu",
        "Purva Ashadha" to "Venus",
        "Uttara Ashadha" to "Sun",
        "Shravana" to "Moon",
        "Dhanishta" to "Mars",
        "Shatabhisha" to "Rahu",
        "Purva Bhadrapada" to "Jupiter",
        "Uttara Bhadrapada" to "Saturn",
        "Revati" to "Mercury"
    )

    val focusAreas: List<String> = listOf(
        "Balanced",
        "Love & Harmony",
        "Career & Purpose",
        "Wellness & Mind",
        "Spiritual Growth"
    )

    val relationshipTypes: List<String> = listOf(
        "Romantic",
        "Platonic",
        "Professional"
    )

    fun findByName(name: String): ZodiacSignInfo {
        val clean = name.trim()
        return allSigns.firstOrNull {
            it.name.equals(clean, ignoreCase = true) ||
                it.sanskritName.equals(clean, ignoreCase = true)
        } ?: allSigns.first()
    }

    fun signIndex(name: String): Int {
        val clean = name.trim()
        val idx = allSigns.indexOfFirst {
            it.name.equals(clean, ignoreCase = true) ||
                it.sanskritName.equals(clean, ignoreCase = true)
        }
        return if (idx >= 0) idx else 0
    }

    /**
     * Computes Sidereal (Lahiri Ayanamsa) Vedic Moon Sign (Chandra Rashi),
     * Sidereal Sun Sign, and Janma Nakshatra + Pada from birth date & time.
     */
    fun calculateVedicMoonDetails(
        birthDate: String,
        birthTime: String = "12:00"
    ): VedicMoonDetails {
        val dateParts = birthDate.trim().split("-")
        if (dateParts.size != 3) {
            return VedicMoonDetails(
                moonSign = allSigns.first(),
                sunSign = allSigns.first(),
                nakshatra = "Ashwini",
                nakshatraLord = "Ketu",
                pada = 1,
                moonDegreeInSign = 0.0
            )
        }
        val yearInput = dateParts[0].toIntOrNull() ?: 1996
        val monthInput = dateParts[1].toIntOrNull() ?: 5
        val dayInput = dateParts[2].toIntOrNull() ?: 14

        val timeParts = birthTime.trim().split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 12
        val minute = timeParts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0

        // Approximate UT offset (-5.5h IST / average or direct local conversion)
        val decimalHours = hour + minute / 60.0 - 5.5
        val dDays = daysSinceJ2000(yearInput, monthInput, dayInput, decimalHours)

        val tropMoon = tropicalMoonLongitudeDeg(dDays)
        val tropSun = tropicalSunLongitudeDeg(dDays)
        val lahiriAyanamsa = 23.853 + (dDays / 365.25) * 0.013969

        val siderealMoon = normalizeDeg(tropMoon - lahiriAyanamsa)
        val siderealSun = normalizeDeg(tropSun - lahiriAyanamsa)

        val moonSignIdx = floor(siderealMoon / 30.0).toInt().coerceIn(0, 11)
        val sunSignIdx = floor(siderealSun / 30.0).toInt().coerceIn(0, 11)

        val nakSpan = 360.0 / 27.0 // 13.333333 deg
        val nakIdx = floor(siderealMoon / nakSpan).toInt().coerceIn(0, 26)
        val padaSpan = nakSpan / 4.0 // 3.333333 deg
        val pada = (floor((siderealMoon - nakIdx * nakSpan) / padaSpan).toInt() + 1).coerceIn(1, 4)

        val (nakName, nakLord) = nakshatras[nakIdx]
        val degreeInSign = siderealMoon - moonSignIdx * 30.0

        return VedicMoonDetails(
            moonSign = allSigns[moonSignIdx],
            sunSign = allSigns[sunSignIdx],
            nakshatra = nakName,
            nakshatraLord = nakLord,
            pada = pada,
            moonDegreeInSign = degreeInSign
        )
    }

    /**
     * Computes Sidereal Moon Sign for an instant epoch millisecond (e.g., current Gochar transit moon).
     */
    fun currentTransitMoonSign(nowMillis: Long = System.currentTimeMillis()): Pair<ZodiacSignInfo, String> {
        val jd = (nowMillis / 86_400_000.0) + 2440587.5
        val dDays = jd - 2451545.0
        val tropMoon = tropicalMoonLongitudeDeg(dDays)
        val lahiriAyanamsa = 23.853 + (dDays / 365.25) * 0.013969
        val siderealMoon = normalizeDeg(tropMoon - lahiriAyanamsa)
        val moonSignIdx = floor(siderealMoon / 30.0).toInt().coerceIn(0, 11)
        val nakIdx = floor(siderealMoon / (360.0 / 27.0)).toInt().coerceIn(0, 26)
        return allSigns[moonSignIdx] to nakshatras[nakIdx].first
    }

    /**
     * Returns the Vedic Moon Sign (Chandra Rashi) for a given birth date/time.
     */
    fun fromBirthDate(birthDate: String, birthTime: String = "12:00"): ZodiacSignInfo {
        return calculateVedicMoonDetails(birthDate, birthTime).moonSign
    }

    fun isValidBirthDate(birthDate: String): Boolean {
        return Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(birthDate)
    }

    private fun daysSinceJ2000(yIn: Int, mIn: Int, dIn: Int, utHour: Double): Double {
        var y = yIn
        var m = mIn
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        val jd0 = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + dIn + b - 1524.5
        return (jd0 + utHour / 24.0) - 2451545.0
    }

    private fun tropicalSunLongitudeDeg(d: Double): Double {
        val l = normalizeDeg(280.460 + 0.9856474 * d)
        val g = normalizeDeg(357.528 + 0.9856003 * d)
        return normalizeDeg(l + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2.0 * g)))
    }

    private fun tropicalMoonLongitudeDeg(d: Double): Double {
        val lPrime = normalizeDeg(218.3164477 + 13.17639648 * d)
        val dElong = normalizeDeg(297.8501921 + 12.19074912 * d)
        val mSun = normalizeDeg(357.5291092 + 0.98560028 * d)
        val mPrime = normalizeDeg(134.9633964 + 13.06499295 * d)
        val fLat = normalizeDeg(93.2720950 + 13.22935024 * d)

        return normalizeDeg(
            lPrime +
                6.288774 * sin(rad(mPrime)) +
                1.274027 * sin(rad(2.0 * dElong - mPrime)) +
                0.658314 * sin(rad(2.0 * dElong)) +
                0.213618 * sin(rad(2.0 * mPrime)) -
                0.185116 * sin(rad(mSun)) -
                0.114332 * sin(rad(2.0 * fLat)) +
                0.058793 * sin(rad(2.0 * dElong - 2.0 * mPrime)) +
                0.057066 * sin(rad(2.0 * dElong - mSun - mPrime)) +
                0.053322 * sin(rad(2.0 * dElong + mPrime)) +
                0.045758 * sin(rad(2.0 * dElong - mSun))
        )
    }

    private val vimshottariSequence: List<Pair<String, Double>> = listOf(
        "Ketu" to 7.0,
        "Venus" to 20.0,
        "Sun" to 6.0,
        "Moon" to 10.0,
        "Mars" to 7.0,
        "Rahu" to 18.0,
        "Jupiter" to 16.0,
        "Saturn" to 19.0,
        "Mercury" to 17.0
    )

    private const val MS_PER_YEAR = 365.2425 * 86_400_000.0

    fun calculateDashaAndGochar(
        birthDate: String,
        birthTime: String = "12:00",
        natalMoonSign: String = "",
        targetMillis: Long = System.currentTimeMillis()
    ): DashaGocharContext {
        val dasha = calculateVimshottariDasha(birthDate, birthTime, targetMillis)
        val effectiveMoon = natalMoonSign.trim().ifBlank {
            calculateVedicMoonDetails(birthDate, birthTime).moonSign.name
        }
        val transits = calculateCurrentGochar(effectiveMoon, targetMillis)
        val isSadeSati = transits.any { it.planet.startsWith("Saturn") && it.specialNote == "Sade Sati" }
        val isChandrashtama = transits.any { it.planet.startsWith("Moon") && it.specialNote == "Chandrashtama" }
        return DashaGocharContext(
            dasha = dasha,
            transits = transits,
            isSadeSati = isSadeSati,
            isChandrashtama = isChandrashtama
        )
    }

    fun calculateVimshottariDasha(
        birthDate: String,
        birthTime: String = "12:00",
        targetMillis: Long = System.currentTimeMillis()
    ): DashaSnapshot {
        val (yearInput, monthInput, dayInput, hour, minute) = parseBirthDateTime(birthDate, birthTime)
        val decimalHours = hour + minute / 60.0 - 5.5
        val dDays = daysSinceJ2000(yearInput, monthInput, dayInput, decimalHours)
        val tropMoon = tropicalMoonLongitudeDeg(dDays)
        val lahiriAyanamsa = 23.853 + (dDays / 365.25) * 0.013969
        val siderealMoon = normalizeDeg(tropMoon - lahiriAyanamsa)

        val nakSpan = 360.0 / 27.0
        val nakIdx = floor(siderealMoon / nakSpan).toInt().coerceIn(0, 26)
        val (nakName, nakLord) = nakshatras[nakIdx]
        val fractionElapsed = ((siderealMoon - nakIdx * nakSpan) / nakSpan).coerceIn(0.0, 0.999999)
        val startLordIdx = vimshottariSequence.indexOfFirst { it.first == nakLord }.coerceAtLeast(0)
        val birthLordYears = vimshottariSequence[startLordIdx].second
        val balanceYears = birthLordYears * (1.0 - fractionElapsed)

        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(yearInput, (monthInput - 1).coerceIn(0, 11), dayInput.coerceIn(1, 31), hour, minute, 0)
        }
        val birthMillis = cal.timeInMillis
        val effectiveTarget = maxOf(targetMillis, birthMillis)

        var cursorStart = birthMillis - (fractionElapsed * birthLordYears * MS_PER_YEAR).toLong()
        var mahaLordIdx = startLordIdx
        var mahaStart = birthMillis
        var mahaNominalStart = cursorStart
        var mahaEnd = cursorStart + (birthLordYears * MS_PER_YEAR).toLong()

        var guard = 0
        while (effectiveTarget >= mahaEnd && guard < 36) {
            mahaNominalStart = mahaEnd
            mahaStart = mahaEnd
            mahaLordIdx = (mahaLordIdx + 1) % vimshottariSequence.size
            val nextYears = vimshottariSequence[mahaLordIdx].second
            mahaEnd = mahaNominalStart + (nextYears * MS_PER_YEAR).toLong()
            guard++
        }

        val mahaLord = vimshottariSequence[mahaLordIdx].first
        val mahaTotalYears = vimshottariSequence[mahaLordIdx].second

        var antarCursor = mahaNominalStart
        var antarLordIdx = mahaLordIdx
        var antarStart = antarCursor
        var antarEnd = antarCursor
        for (step in 0 until vimshottariSequence.size) {
            val idx = (mahaLordIdx + step) % vimshottariSequence.size
            val antarYears = (mahaTotalYears * vimshottariSequence[idx].second) / 120.0
            val nextEnd = antarCursor + (antarYears * MS_PER_YEAR).toLong()
            if (effectiveTarget < nextEnd || step == vimshottariSequence.lastIndex) {
                antarLordIdx = idx
                antarStart = maxOf(antarCursor, birthMillis)
                antarEnd = nextEnd
                break
            }
            antarCursor = nextEnd
        }

        return DashaSnapshot(
            mahadashaLord = mahaLord,
            mahadashaStart = formatMonthYear(mahaStart),
            mahadashaEnd = formatMonthYear(mahaEnd),
            antardashaLord = vimshottariSequence[antarLordIdx].first,
            antardashaStart = formatMonthYear(antarStart),
            antardashaEnd = formatMonthYear(antarEnd),
            birthNakshatra = nakName,
            birthNakshatraLord = nakLord,
            balanceYearsAtBirth = balanceYears
        )
    }

    fun calculateCurrentGochar(
        natalMoonSign: String,
        targetMillis: Long = System.currentTimeMillis()
    ): List<GocharTransit> {
        val natalIdx = signIndex(natalMoonSign)
        val jd = (targetMillis / 86_400_000.0) + 2440587.5
        val d = jd - 2451545.0
        val lahiri = 23.853 + (d / 365.25) * 0.013969

        val tropSun = tropicalSunLongitudeDeg(d)
        val tropMoon = tropicalMoonLongitudeDeg(d)
        val gSun = normalizeDeg(357.528 + 0.9856003 * d)
        val rSun = 1.00014 - 0.01671 * cos(rad(gSun)) - 0.00014 * cos(rad(2.0 * gSun))
        val earthX = -rSun * cos(rad(tropSun))
        val earthY = -rSun * sin(rad(tropSun))

        fun geocentricSidereal(nDeg: Double, iDeg: Double, wDeg: Double, a: Double, e: Double, mDeg: Double): Double {
            val mRad = rad(normalizeDeg(mDeg))
            var eAnom = mRad + e * sin(mRad) * (1.0 + e * cos(mRad))
            eAnom -= (eAnom - e * sin(eAnom) - mRad) / (1.0 - e * cos(eAnom))
            val xv = a * (cos(eAnom) - e)
            val yv = a * sqrt(1.0 - e * e) * sin(eAnom)
            val v = atan2(yv, xv)
            val r = hypot(xv, yv)
            val n = rad(nDeg)
            val i = rad(iDeg)
            val vw = v + rad(wDeg)
            val xh = r * (cos(n) * cos(vw) - sin(n) * sin(vw) * cos(i))
            val yh = r * (sin(n) * cos(vw) + cos(n) * sin(vw) * cos(i))
            val tropLon = normalizeDeg(atan2(yh - earthY, xh - earthX) * 180.0 / PI)
            return normalizeDeg(tropLon - lahiri)
        }

        val sidMoon = normalizeDeg(tropMoon - lahiri)
        val sidSun = normalizeDeg(tropSun - lahiri)
        val sidMercury = geocentricSidereal(48.3313 + 3.24587e-5 * d, 7.0047 + 5.00e-8 * d, 29.1241 + 1.01444e-5 * d, 0.387098, 0.205635 + 5.59e-10 * d, 168.6562 + 4.0923344368 * d)
        val sidVenus = geocentricSidereal(76.6799 + 2.46590e-5 * d, 3.3946 + 2.75e-8 * d, 54.8910 + 1.38374e-5 * d, 0.723330, 0.006773 - 1.302e-9 * d, 48.0052 + 1.6021302244 * d)
        val sidMars = geocentricSidereal(49.5574 + 2.11081e-5 * d, 1.8497 - 1.78e-8 * d, 286.5016 + 2.92961e-5 * d, 1.523688, 0.093405 + 2.516e-9 * d, 18.6021 + 0.5240207766 * d)
        val sidJupiter = geocentricSidereal(100.4542 + 2.76854e-5 * d, 1.3030 - 1.557e-7 * d, 273.8777 + 1.64505e-5 * d, 5.20256, 0.048498 + 4.469e-9 * d, 19.8950 + 0.0830853001 * d)
        val sidSaturn = geocentricSidereal(113.6634 + 2.38980e-5 * d, 2.4886 - 1.081e-7 * d, 339.3939 + 2.97661e-5 * d, 9.55475, 0.055546 - 9.499e-9 * d, 316.9670 + 0.0334442282 * d)
        val sidRahu = normalizeDeg((125.04452 - 0.05295377 * d) - lahiri)
        val sidKetu = normalizeDeg(sidRahu + 180.0)

        val entries = listOf(
            "Moon (Chandra)" to sidMoon,
            "Jupiter (Guru)" to sidJupiter,
            "Saturn (Shani)" to sidSaturn,
            "Rahu" to sidRahu,
            "Ketu" to sidKetu,
            "Sun (Surya)" to sidSun,
            "Mars (Mangala)" to sidMars,
            "Mercury (Budha)" to sidMercury,
            "Venus (Shukra)" to sidVenus
        )

        return entries.map { (planet, sidLon) ->
            val signIdx = floor(sidLon / 30.0).toInt().coerceIn(0, 11)
            val houseFromMoon = ((signIdx - natalIdx + 12) % 12) + 1
            val (favorability, note) = evaluateGocharHouse(planet, houseFromMoon)
            GocharTransit(
                planet = planet,
                transitSign = allSigns[signIdx],
                houseFromMoon = houseFromMoon,
                favorability = favorability,
                specialNote = note
            )
        }
    }

    private fun evaluateGocharHouse(planet: String, houseFromMoon: Int): Pair<String, String> {
        val base = planet.substringBefore(" ").lowercase(Locale.US)
        return when (base) {
            "moon" -> when (houseFromMoon) {
                1, 3, 6, 7, 10, 11 -> "Favorable" to ""
                8 -> "Challenging" to "Chandrashtama"
                else -> "Neutral" to ""
            }
            "sun" -> if (houseFromMoon in setOf(3, 6, 10, 11)) "Favorable" to "" else if (houseFromMoon in setOf(8, 12)) "Challenging" to "" else "Neutral" to ""
            "mars" -> if (houseFromMoon in setOf(3, 6, 11)) "Favorable" to "" else if (houseFromMoon in setOf(1, 7, 8, 12)) "Challenging" to "" else "Neutral" to ""
            "mercury" -> if (houseFromMoon in setOf(2, 4, 6, 8, 10, 11)) "Favorable" to "" else "Neutral" to ""
            "jupiter" -> if (houseFromMoon in setOf(2, 5, 7, 9, 11)) "Favorable" to "" else if (houseFromMoon in setOf(3, 6, 8, 12)) "Challenging" to "" else "Neutral" to ""
            "venus" -> if (houseFromMoon in setOf(1, 2, 3, 4, 5, 8, 9, 11, 12)) "Favorable" to "" else "Neutral" to ""
            "saturn" -> when (houseFromMoon) {
                3, 6, 11 -> "Favorable" to ""
                12, 1, 2 -> "Challenging" to "Sade Sati"
                4, 8 -> "Challenging" to "Shani Dhaiya"
                else -> "Neutral" to ""
            }
            else -> if (houseFromMoon in setOf(3, 6, 11)) "Favorable" to "" else if (houseFromMoon in setOf(1, 2, 7, 8, 12)) "Challenging" to "" else "Neutral" to ""
        }
    }

    private fun parseBirthDateTime(birthDate: String, birthTime: String): IntArray {
        val dateParts = birthDate.trim().split("-")
        val yearInput = dateParts.getOrNull(0)?.toIntOrNull() ?: 1996
        val monthInput = dateParts.getOrNull(1)?.toIntOrNull() ?: 5
        val dayInput = dateParts.getOrNull(2)?.toIntOrNull() ?: 14
        val timeParts = birthTime.trim().split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 12
        val minute = timeParts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return intArrayOf(yearInput, monthInput, dayInput, hour, minute)
    }

    private fun formatMonthYear(millis: Long): String {
        val fmt = SimpleDateFormat("MMM yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return fmt.format(Date(millis))
    }

    private fun rad(deg: Double): Double = deg * PI / 180.0

    private fun normalizeDeg(deg: Double): Double {
        val mod = deg % 360.0
        return if (mod < 0.0) mod + 360.0 else mod
    }
}
