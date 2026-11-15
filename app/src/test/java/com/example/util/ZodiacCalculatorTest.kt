package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZodiacCalculatorTest {

    @Test
    fun calculateVedicMoonDetails_computesSiderealMoonSignAndNakshatra() {
        // 1996-05-14 08:00 local -> Sidereal Moon is in Aries (Mesha) / Pisces boundary (around Ashwini/Revati depending on exact UT)
        val details = ZodiacCalculator.calculateVedicMoonDetails("1996-05-14", "08:00")
        assertTrue(details.moonSign.name in ZodiacCalculator.allSigns.map { it.name })
        assertTrue(details.moonSign.sanskritName.isNotBlank())
        assertTrue(details.nakshatra in ZodiacCalculator.nakshatras.map { it.first })
        assertTrue(details.pada in 1..4)
        assertTrue(details.moonDegreeInSign in 0.0..30.0)
        // Sidereal Sun on May 14 is in late Mesha (Aries) or early Vrishabha (Taurus) (~29 deg Mesha)
        assertTrue(details.sunSign.name == "Aries" || details.sunSign.name == "Taurus")
    }

    @Test
    fun findByName_resolvesBothEnglishAndSanskritRashiNames() {
        val mesha = ZodiacCalculator.findByName("Mesha")
        val aries = ZodiacCalculator.findByName("Aries")
        assertEquals("Aries", mesha.name)
        assertEquals("Mesha", aries.sanskritName)

        val vrishchika = ZodiacCalculator.findByName("Vrishchika")
        assertEquals("Scorpio", vrishchika.name)
        assertEquals(7, ZodiacCalculator.signIndex("Vrishchika"))
    }

    @Test
    fun currentTransitMoonSign_returnsValidGocharRashiAndNakshatra() {
        val (sign, nakshatra) = ZodiacCalculator.currentTransitMoonSign(1763164800000L) // Fixed timestamp
        assertTrue(sign in ZodiacCalculator.allSigns)
        assertTrue(nakshatra in ZodiacCalculator.nakshatras.map { it.first })
    }

    @Test
    fun calculateDashaAndGochar_computesMahadashaAntardashaAndNineTransits() {
        val targetMillis = 1794700800000L // 2026-11-15 UTC
        val ctx = ZodiacCalculator.calculateDashaAndGochar(
            birthDate = "1996-05-14",
            birthTime = "08:00",
            natalMoonSign = "Aries",
            targetMillis = targetMillis
        )
        assertTrue(ctx.dasha.mahadashaLord.isNotBlank())
        assertTrue(ctx.dasha.antardashaLord.isNotBlank())
        assertTrue(ctx.dasha.balanceYearsAtBirth in 0.0..20.0)
        assertEquals(9, ctx.transits.size)
        assertTrue(ctx.transits.all { it.houseFromMoon in 1..12 })
        // Rahu in Nov 2026 is in Sidereal Aquarius (11th house from Aries Moon) -> Favorable
        val rahuTransit = ctx.transits.first { it.planet == "Rahu" }
        assertEquals("Aquarius", rahuTransit.transitSign.name)
        assertEquals(11, rahuTransit.houseFromMoon)
        assertEquals("Favorable", rahuTransit.favorability)
    }
}
