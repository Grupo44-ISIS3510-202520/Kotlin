package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.source.daos.ReportDao
import com.example.brigadeapp.data.source.local.AppDatabase
import com.example.brigadeapp.data.source.local.leaderboard.LeaderboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = AppDatabase.getInstance(context)

    @Provides
    fun provideReportDao(
        db: AppDatabase
    ): ReportDao = db.reportDao()

    @Provides
    fun provideLeaderboardDao(
        db: AppDatabase
    ): LeaderboardDao = db.leaderboardDao()
}
