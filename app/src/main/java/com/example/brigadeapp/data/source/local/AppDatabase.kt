package com.example.brigadeapp.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.brigadeapp.data.source.daos.ReportDao
import com.example.brigadeapp.data.source.daos.CachedReportDao
import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.data.source.local.leaderboard.LeaderboardDao
import com.example.brigadeapp.data.source.local.leaderboard.LeaderboardEntryEntity


@Database(
    entities = [
        Report::class,
        CachedReport::class,
        LeaderboardEntryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun cachedReportDao(): CachedReportDao
    abstract fun leaderboardDao(): LeaderboardDao


    companion object {
        private const val DB_NAME = "brigade_app_db"
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add synced column to cached_reports table
                // Default to 1 (true) for existing reports - they came from Firebase so they're already synced
                database.execSQL("ALTER TABLE cached_reports ADD COLUMN synced INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}