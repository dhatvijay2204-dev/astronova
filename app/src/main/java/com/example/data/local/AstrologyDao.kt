package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AstrologyDao {

    @Query("SELECT * FROM user_profile WHERE id = 'local_profile' LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM horoscopes ORDER BY updatedAtMs DESC")
    fun observeHoroscopes(): Flow<List<HoroscopeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHoroscope(reading: HoroscopeEntity)

    @Query("SELECT * FROM affirmations ORDER BY updatedAtMs DESC")
    fun observeAffirmations(): Flow<List<AffirmationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAffirmation(affirmation: AffirmationEntity)

    @Query("UPDATE affirmations SET isFavorite = :isFavorite, updatedAtMs = :updatedAtMs WHERE id = :affirmationId")
    suspend fun updateAffirmationFavorite(affirmationId: String, isFavorite: Boolean, updatedAtMs: Long)

    @Query("DELETE FROM affirmations WHERE id = :affirmationId")
    suspend fun deleteAffirmationById(affirmationId: String)

    @Query("SELECT * FROM birth_charts ORDER BY updatedAtMs DESC")
    fun observeBirthCharts(): Flow<List<BirthChartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBirthChart(chart: BirthChartEntity)

    @Query("SELECT * FROM compatibilities ORDER BY updatedAtMs DESC")
    fun observeCompatibilities(): Flow<List<CompatibilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompatibility(compatibility: CompatibilityEntity)
}
