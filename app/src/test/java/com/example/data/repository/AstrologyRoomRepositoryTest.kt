package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AstrologyDatabase
import com.example.model.BirthChartReading
import com.example.model.CompatibilityReading
import com.example.model.DailyAffirmation
import com.example.model.HoroscopeReading
import com.example.model.PlanetaryPlacement
import com.example.model.UserAstrologyProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AstrologyRoomRepositoryTest {

    private lateinit var database: AstrologyDatabase
    private lateinit var repository: AstrologyRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AstrologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AstrologyRepository(database.astrologyDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveAndObserveProfile_persistsVedicMoonDetailsLocally() = runBlocking {
        val profile = UserAstrologyProfile(
            fullName = "Maya Lin",
            birthDate = "1996-05-14",
            birthTime = "07:30",
            birthPlace = "Seattle, USA",
            zodiacSign = "Aries",
            vedicRashiName = "Mesha",
            nakshatra = "Ashwini",
            nakshatraPada = 2,
            sunSign = "Taurus",
            isMoonSignManual = false,
            element = "Fire",
            focusArea = "Balanced",
            notificationsEnabled = true,
            notificationHour = 8L,
            notificationMinute = 15L
        )
        assertTrue(repository.saveProfile(profile).isSuccess)

        val observed = repository.observeProfile().first { it != null }
        assertNotNull(observed)
        assertEquals("Maya Lin", observed?.fullName)
        assertEquals("Aries", observed?.zodiacSign)
        assertEquals("Mesha", observed?.vedicRashiName)
        assertEquals("Ashwini", observed?.nakshatra)
        assertEquals(2, observed?.nakshatraPada)
        assertEquals("Taurus", observed?.sunSign)
        assertEquals(false, observed?.isMoonSignManual)
    }

    @Test
    fun saveAndObserveHoroscopeAndAffirmation_togglesFavorite() = runBlocking {
        val reading = HoroscopeReading(
            periodType = "daily",
            periodKey = "2026-11-14",
            zodiacSign = "Aries",
            nakshatra = "Ashwini • Pada 2",
            headline = "Lunar Clarity in Mesha",
            overview = "Gochar Chandra highlights steady focus.",
            loveInsight = "Warmth builds deeper trust.",
            careerInsight = "Steady steps pay off.",
            wellnessInsight = "Rest well tonight.",
            luckyNumber = 6L,
            luckyColor = "Champagne Gold",
            powerPlanet = "Mars",
            cosmicScore = 88L,
            jobPrecautions = listOf(
                "Avoid rushing contract sign-offs before reviewing fine print.",
                "Keep workplace discussions factual during tense meetings."
            ),
            lifePrecautions = listOf(
                "Avoid impulsive purchases late in the evening.",
                "Practice speech restraint in sensitive family conversations."
            ),
            dashaGocharNote = "Jupiter Mahadasha supports wisdom while Moon in 8th signals caution."
        )
        assertTrue(repository.saveHoroscope("daily_2026-11-14_aries_en", reading).isSuccess)
        val horoscopes = repository.observeHoroscopes().first { it.isNotEmpty() }
        assertEquals("Lunar Clarity in Mesha", horoscopes.first().headline)
        assertEquals("Ashwini • Pada 2", horoscopes.first().nakshatra)
        assertEquals(2, horoscopes.first().jobPrecautions.size)
        assertEquals(2, horoscopes.first().lifePrecautions.size)
        assertEquals(
            "Avoid rushing contract sign-offs before reviewing fine print.",
            horoscopes.first().jobPrecautions.first()
        )
        assertEquals(
            "Jupiter Mahadasha supports wisdom while Moon in 8th signals caution.",
            horoscopes.first().dashaGocharNote
        )

        val aff = DailyAffirmation(
            dateKey = "2026-11-14",
            text = "I trust my inner compass.",
            ritualPrompt = "Take three slow breaths.",
            zodiacSign = "Aries",
            focusArea = "Confidence",
            isFavorite = false
        )
        val affId = repository.saveAffirmation("aff_1", aff).getOrThrow()
        assertTrue(repository.toggleAffirmationFavorite(affId, true).isSuccess)
        val savedAffs = repository.observeAffirmations().first { list -> list.any { it.id == affId && it.isFavorite } }
        assertTrue(savedAffs.any { it.id == affId && it.isFavorite })
    }

    @Test
    fun saveBirthChartAndCompatibility_roundTripsPlacementsAndScores() = runBlocking {
        val chart = BirthChartReading(
            fullName = "Maya Lin",
            birthDate = "1996-05-14",
            birthTime = "07:30",
            birthPlace = "Seattle, USA",
            sunSign = "Taurus",
            moonSign = "Aries",
            risingSign = "Gemini",
            nakshatra = "Ashwini (Pada 2)",
            chartSummary = "Chandra in Mesha brings swift clarity.",
            activeDasha = "Venus Mahadasha • Mercury Antardasha",
            dashaEffect = "Venus activates 5th house creativity in this Kundali.",
            gocharEffect = "Rahu in 11th house from Chandra Rashi expands network gains.",
            placements = listOf(
                PlanetaryPlacement("Moon", "Aries", 11L, 11L, "Warm emotional drive."),
                PlanetaryPlacement("Sun", "Taurus", 24L, 12L, "Quiet inner resolve."),
                PlanetaryPlacement("Rahu", "Virgo", 18L, 4L, "Deep analytical focus.")
            )
        )
        assertTrue(repository.saveBirthChart("chart_1", chart).isSuccess)
        val charts = repository.observeBirthCharts().first { it.isNotEmpty() }
        assertEquals(3, charts.first().placements.size)
        assertEquals("Gemini", charts.first().risingSign)
        assertEquals("Ashwini (Pada 2)", charts.first().nakshatra)
        assertEquals("Venus Mahadasha • Mercury Antardasha", charts.first().activeDasha)
        assertEquals("Venus activates 5th house creativity in this Kundali.", charts.first().dashaEffect)
        assertEquals("Rahu in 11th house from Chandra Rashi expands network gains.", charts.first().gocharEffect)

        val compat = CompatibilityReading(
            signOne = "Taurus",
            signTwo = "Scorpio",
            relationshipType = "Romantic",
            overallScore = 89L,
            romanticScore = 93L,
            platonicScore = 82L,
            professionalScore = 86L,
            summary = "Deep magnetic resonance.",
            strengths = "Loyal devotion.",
            challenges = "Stubbornness."
        )
        assertTrue(repository.saveCompatibility("compat_1", compat).isSuccess)
        val compats = repository.observeCompatibilities().first { it.isNotEmpty() }
        assertEquals(89L, compats.first().overallScore)
    }
}
