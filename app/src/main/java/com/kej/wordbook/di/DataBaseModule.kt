package com.kej.wordbook.di

import android.content.Context
import androidx.room.Room
import com.kej.wordbook.data.database.AppDatabase
import com.kej.wordbook.data.database.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataBaseModule {
    @Provides
    @Singleton
    fun getInstance(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }
}