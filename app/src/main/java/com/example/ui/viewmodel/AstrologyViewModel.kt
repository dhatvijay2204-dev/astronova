package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAstrologyService
import com.example.data.repository.AstrologyRepository
import com.example.localization.AppLanguage
import com.example.model.BirthChartReading
import com.example.model.CompatibilityReading
import com.example.model.DailyAffirmation
import com.example.model.HoroscopeReading
import com.example.model.UiState
import com.example.model.UserAstrologyProfile
import com.example.util.ZodiacCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AstrologyViewModel(
    private val repository: AstrologyRepository,
    private val aiService: AiAstrologyService
) : ViewModel() {

    val profileState: StateFlow<UiState<UserAstrologyProfile?>> = repository.observeProfile()
        .map<UserAstrologyProfile?, UiState<UserAstrologyProfile?>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing profile", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val horoscopesState: StateFlow<UiState<List<HoroscopeReading>>> = repository.observeHoroscopes()
        .map<List<HoroscopeReading>, UiState<List<HoroscopeReading>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing horoscopes", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load horoscopes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val affirmationsState: StateFlow<UiState<List<DailyAffirmation>>> = repository.observeAffirmations()
        .map<List<DailyAffirmation>, UiState<List<DailyAffirmation>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing affirmations", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load affirmations"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val birthChartsState: StateFlow<UiState<List<BirthChartReading>>> = repository.observeBirthCharts()
        .map<List<BirthChartReading>, UiState<List<BirthChartReading>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing birth charts", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load birth charts"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val compatibilitiesState: StateFlow<UiState<List<CompatibilityReading>>> = repository.observeCompatibilities()
        .map<List<CompatibilityReading>, UiState<List<CompatibilityReading>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing compatibilities", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load compatibility history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _selectedPeriod = MutableStateFlow("daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _horoscopeGenState = MutableStateFlow<UiState<HoroscopeReading>>(UiState.Idle)
    val horoscopeGenState: StateFlow<UiState<HoroscopeReading>> = _horoscopeGenState.asStateFlow()

    private val _affirmationGenState = MutableStateFlow<UiState<DailyAffirmation>>(UiState.Idle)
    val affirmationGenState: StateFlow<UiState<DailyAffirmation>> = _affirmationGenState.asStateFlow()

    private val _birthChartGenState = MutableStateFlow<UiState<BirthChartReading>>(UiState.Idle)
    val birthChartGenState: StateFlow<UiState<BirthChartReading>> = _birthChartGenState.asStateFlow()

    private val _compatibilityGenState = MutableStateFlow<UiState<CompatibilityReading>>(UiState.Idle)
    val compatibilityGenState: StateFlow<UiState<CompatibilityReading>> = _compatibilityGenState.asStateFlow()

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun selectPeriod(period: String) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
            if (_horoscopeGenState.value !is UiState.Loading) {
                _horoscopeGenState.value = UiState.Idle
            }
        }
    }

    fun currentDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun currentMonthKey(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    fun saveProfile(
        fullName: String,
        birthDate: String,
        birthTime: String,
        birthPlace: String,
        focusArea: String,
        notificationsEnabled: Boolean,
        notificationHour: Int,
        notificationMinute: Int,
        manualMoonSign: String? = null,
        onComplete: () -> Unit = {}
    ) {
        val vedic = ZodiacCalculator.calculateVedicMoonDetails(birthDate, birthTime)
        val isManual = !manualMoonSign.isNullOrBlank()
        val chosenMoon = if (isManual) ZodiacCalculator.findByName(manualMoonSign!!) else vedic.moonSign
        val profile = UserAstrologyProfile(
            id = LOCAL_PROFILE_ID,
            userId = LOCAL_USER_ID,
            fullName = fullName.trim().ifBlank { "Stargazer" },
            birthDate = birthDate.trim(),
            birthTime = birthTime.trim().ifBlank { "Unknown" },
            birthPlace = birthPlace.trim().ifBlank { "Earth" },
            zodiacSign = chosenMoon.name,
            vedicRashiName = chosenMoon.sanskritName,
            nakshatra = vedic.nakshatra,
            nakshatraPada = vedic.pada,
            sunSign = vedic.sunSign.name,
            isMoonSignManual = isManual,
            element = chosenMoon.element,
            focusArea = focusArea,
            notificationsEnabled = notificationsEnabled,
            notificationHour = notificationHour.toLong(),
            notificationMinute = notificationMinute.toLong()
        )
        viewModelScope.launch {
            _isSavingProfile.value = true
            val result = repository.saveProfile(profile)
            _isSavingProfile.value = false
            result.onSuccess {
                onComplete()
            }.onFailure { error ->
                _statusBanner.value = error.localizedMessage ?: "Failed to save birth profile."
            }
        }
    }

    fun updateNotificationPrefs(
        existingProfile: UserAstrologyProfile,
        enabled: Boolean,
        hour: Int,
        minute: Int
    ) {
        val updated = existingProfile.copy(
            notificationsEnabled = enabled,
            notificationHour = hour.toLong(),
            notificationMinute = minute.toLong()
        )
        viewModelScope.launch {
            repository.saveProfile(updated).onFailure { error ->
                _statusBanner.value = error.localizedMessage ?: "Failed to update notification settings."
            }
        }
    }

    fun generateHoroscope(
        profile: UserAstrologyProfile,
        periodType: String,
        language: AppLanguage
    ) {
        val periodKey = if (periodType == "monthly") currentMonthKey() else currentDateKey()
        val readingId = "${periodType}_${periodKey}_${profile.zodiacSign}_${language.code}"
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9_\\-]"), "_")
        viewModelScope.launch {
            _horoscopeGenState.value = UiState.Loading
            try {
                val reading = aiService.generateHoroscope(profile, periodType, periodKey, language)
                repository.saveHoroscope(readingId, reading)
                _horoscopeGenState.value = UiState.Success(reading)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Horoscope generation timed out", e)
                _horoscopeGenState.value = UiState.Error("Horoscope reading timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Horoscope generation failed: ${e.cause?.message ?: e.message}", e)
                _horoscopeGenState.value = UiState.Error(formatAiError(e, "Failed to generate horoscope."))
            }
        }
    }

    fun generateAffirmation(
        profile: UserAstrologyProfile,
        intention: String,
        language: AppLanguage,
        onGenerated: (DailyAffirmation) -> Unit = {}
    ) {
        val dateKey = currentDateKey()
        val affirmationId = "aff_${dateKey}_${UUID.randomUUID().toString().take(8)}"
        viewModelScope.launch {
            _affirmationGenState.value = UiState.Loading
            try {
                val affirmation = aiService.generateAffirmation(profile, intention, dateKey, language)
                val savedId = repository.saveAffirmation(affirmationId, affirmation).getOrElse { affirmationId }
                val stored = affirmation.copy(id = savedId)
                _affirmationGenState.value = UiState.Success(stored)
                onGenerated(stored)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Affirmation generation timed out", e)
                _affirmationGenState.value = UiState.Error("Affirmation generation timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Affirmation generation failed: ${e.cause?.message ?: e.message}", e)
                _affirmationGenState.value = UiState.Error(formatAiError(e, "Failed to generate affirmation."))
            }
        }
    }

    fun toggleFavorite(affirmation: DailyAffirmation) {
        if (affirmation.id.isBlank()) return
        viewModelScope.launch {
            repository.toggleAffirmationFavorite(affirmation.id, !affirmation.isFavorite)
                .onFailure { error ->
                    _statusBanner.value = error.localizedMessage ?: "Could not update favorite."
                }
        }
    }

    fun deleteAffirmation(affirmationId: String) {
        if (affirmationId.isBlank()) return
        viewModelScope.launch {
            repository.deleteAffirmation(affirmationId)
                .onFailure { error ->
                    _statusBanner.value = error.localizedMessage ?: "Could not delete affirmation."
                }
        }
    }

    fun generateBirthChart(
        fullName: String,
        birthDate: String,
        birthTime: String,
        birthPlace: String,
        language: AppLanguage,
        manualMoonSign: String? = null
    ) {
        val cleanTime = birthTime.trim().ifBlank { "12:00" }
        val vedic = ZodiacCalculator.calculateVedicMoonDetails(birthDate, cleanTime)
        val moonSign = if (!manualMoonSign.isNullOrBlank()) {
            ZodiacCalculator.findByName(manualMoonSign).name
        } else {
            vedic.moonSign.name
        }
        val sunSign = vedic.sunSign.name
        val nakshatraLabel = "${vedic.nakshatra} (Pada ${vedic.pada})"
        val cleanPlace = birthPlace.trim().ifBlank { "Earth" }
        val cleanName = fullName.trim().ifBlank { "Stargazer" }
        val chartId = "chart_${birthDate}_${cleanTime}_${moonSign}_${language.code}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")

        viewModelScope.launch {
            _birthChartGenState.value = UiState.Loading
            try {
                val chart = aiService.generateBirthChart(
                    userId = LOCAL_USER_ID,
                    fullName = cleanName,
                    birthDate = birthDate,
                    birthTime = cleanTime,
                    birthPlace = cleanPlace,
                    moonSign = moonSign,
                    sunSign = sunSign,
                    nakshatra = nakshatraLabel,
                    language = language
                )
                repository.saveBirthChart(chartId, chart)
                _birthChartGenState.value = UiState.Success(chart)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Birth chart generation timed out", e)
                _birthChartGenState.value = UiState.Error("Birth chart calculation timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Birth chart generation failed: ${e.cause?.message ?: e.message}", e)
                _birthChartGenState.value = UiState.Error(formatAiError(e, "Failed to generate birth chart."))
            }
        }
    }

    fun analyzeCompatibility(
        signOne: String,
        signTwo: String,
        relationshipType: String,
        language: AppLanguage
    ) {
        val compatId = "${signOne}_${signTwo}_${relationshipType}_${language.code}"
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9_\\-]"), "_")
        viewModelScope.launch {
            _compatibilityGenState.value = UiState.Loading
            try {
                val reading = aiService.generateCompatibility(
                    userId = LOCAL_USER_ID,
                    signOne = signOne,
                    signTwo = signTwo,
                    relationshipType = relationshipType,
                    language = language
                )
                repository.saveCompatibility(compatId, reading)
                _compatibilityGenState.value = UiState.Success(reading)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Compatibility generation timed out", e)
                _compatibilityGenState.value = UiState.Error("Compatibility analysis timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Compatibility generation failed: ${e.cause?.message ?: e.message}", e)
                _compatibilityGenState.value = UiState.Error(formatAiError(e, "Failed to analyze compatibility."))
            }
        }
    }

    fun analyzePersonCompatibility(
        profile: UserAstrologyProfile,
        partnerName: String,
        partnerBirthDate: String,
        partnerBirthTime: String,
        partnerBirthPlace: String,
        relationshipType: String,
        language: AppLanguage
    ) {
        val partnerVedic = ZodiacCalculator.calculateVedicMoonDetails(partnerBirthDate, partnerBirthTime)
        val partnerMoonSign = partnerVedic.moonSign.name
        val partnerNakshatra = "${partnerVedic.nakshatra} (Pada ${partnerVedic.pada})"
        val userNakshatra = if (profile.nakshatra.isNotBlank()) "${profile.nakshatra} (Pada ${profile.nakshatraPada})" else "Janma Nakshatra"
        val compatId = "person_${profile.zodiacSign}_${partnerMoonSign}_${relationshipType}_${language.code}"
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9_\\-]"), "_")

        viewModelScope.launch {
            _compatibilityGenState.value = UiState.Loading
            try {
                val reading = aiService.generatePersonCompatibility(
                    userId = LOCAL_USER_ID,
                    personOneName = profile.fullName,
                    personOneBirthDate = profile.birthDate,
                    personOneBirthTime = profile.birthTime,
                    personOneBirthPlace = profile.birthPlace,
                    personOneMoonSign = profile.zodiacSign,
                    personOneNakshatra = userNakshatra,
                    personTwoName = partnerName.trim().ifBlank { "Partner" },
                    personTwoBirthDate = partnerBirthDate.trim(),
                    personTwoBirthTime = partnerBirthTime.trim().ifBlank { "Unknown" },
                    personTwoBirthPlace = partnerBirthPlace.trim().ifBlank { "Earth" },
                    personTwoMoonSign = partnerMoonSign,
                    personTwoNakshatra = partnerNakshatra,
                    relationshipType = relationshipType,
                    language = language
                )
                repository.saveCompatibility(compatId, reading)
                _compatibilityGenState.value = UiState.Success(reading)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Person compatibility generation timed out", e)
                _compatibilityGenState.value = UiState.Error("Compatibility report timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Person compatibility generation failed: ${e.cause?.message ?: e.message}", e)
                _compatibilityGenState.value = UiState.Error(formatAiError(e, "Failed to analyze compatibility."))
            }
        }
    }

    private fun formatAiError(e: Throwable, fallback: String): String {
        val root = generateSequence(e) { it.cause }.lastOrNull()
        val rootMsg = root?.localizedMessage?.takeIf { it.isNotBlank() && it != e.localizedMessage }
        return if (rootMsg != null) {
            "${e.localizedMessage ?: fallback} ($rootMsg)"
        } else {
            e.localizedMessage ?: fallback
        }
    }

    private companion object {
        const val TAG = "AstrologyVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
        const val LOCAL_PROFILE_ID = "local_profile"
        const val LOCAL_USER_ID = "local_user"
    }
}
