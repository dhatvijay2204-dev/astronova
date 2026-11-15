package com.example.data.repository

import com.example.data.local.AffirmationEntity
import com.example.data.local.AstrologyDao
import com.example.data.local.BirthChartEntity
import com.example.data.local.CompatibilityEntity
import com.example.data.local.HoroscopeEntity
import com.example.data.local.UserProfileEntity
import com.example.model.BirthChartReading
import com.example.model.CompatibilityReading
import com.example.model.DailyAffirmation
import com.example.model.HoroscopeReading
import com.example.model.UserAstrologyProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AstrologyRepository(
    private val dao: AstrologyDao
) {
    fun observeProfile(): Flow<UserAstrologyProfile?> =
        dao.observeProfile().map { it?.toDomain() }

    suspend fun saveProfile(profile: UserAstrologyProfile): Result<Unit> {
        return try {
            dao.upsertProfile(UserProfileEntity.fromDomain(profile))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeHoroscopes(): Flow<List<HoroscopeReading>> =
        dao.observeHoroscopes().map { list -> list.map { it.toDomain() } }

    suspend fun saveHoroscope(readingId: String, reading: HoroscopeReading): Result<Unit> {
        return try {
            val safeId = sanitizeId(readingId)
            dao.upsertHoroscope(HoroscopeEntity.fromDomain(safeId, reading))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAffirmations(): Flow<List<DailyAffirmation>> =
        dao.observeAffirmations().map { list -> list.map { it.toDomain() } }

    suspend fun saveAffirmation(affirmationId: String, affirmation: DailyAffirmation): Result<String> {
        return try {
            val safeId = sanitizeId(affirmationId)
            dao.upsertAffirmation(AffirmationEntity.fromDomain(safeId, affirmation))
            Result.success(safeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleAffirmationFavorite(affirmationId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            dao.updateAffirmationFavorite(affirmationId, isFavorite, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAffirmation(affirmationId: String): Result<Unit> {
        return try {
            dao.deleteAffirmationById(affirmationId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeBirthCharts(): Flow<List<BirthChartReading>> =
        dao.observeBirthCharts().map { list -> list.map { it.toDomain() } }

    suspend fun saveBirthChart(chartId: String, chart: BirthChartReading): Result<Unit> {
        return try {
            val safeId = sanitizeId(chartId)
            dao.upsertBirthChart(BirthChartEntity.fromDomain(safeId, chart))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeCompatibilities(): Flow<List<CompatibilityReading>> =
        dao.observeCompatibilities().map { list -> list.map { it.toDomain() } }

    suspend fun saveCompatibility(compatId: String, reading: CompatibilityReading): Result<Unit> {
        return try {
            val safeId = sanitizeId(compatId)
            dao.upsertCompatibility(CompatibilityEntity.fromDomain(safeId, reading))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { "doc_default" }.take(120)
    }
}
