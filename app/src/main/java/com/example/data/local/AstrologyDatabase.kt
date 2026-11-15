package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        HoroscopeEntity::class,
        AffirmationEntity::class,
        BirthChartEntity::class,
        CompatibilityEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AstrologyDatabase : RoomDatabase() {
    abstract fun astrologyDao(): AstrologyDao

    companion object {
        @Volatile
        private var INSTANCE: AstrologyDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horoscopes ADD COLUMN jobPrecautionsJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE horoscopes ADD COLUMN lifePrecautionsJson TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE horoscopes ADD COLUMN dashaGocharNote TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE birth_charts ADD COLUMN activeDasha TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE birth_charts ADD COLUMN dashaEffect TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE birth_charts ADD COLUMN gocharEffect TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AstrologyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AstrologyDatabase::class.java,
                    "astranova_local.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
