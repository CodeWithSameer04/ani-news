package com.anime.tracker.di

import android.content.Context
import androidx.room.Room
import com.anime.tracker.data.api.AniListApiService
import com.anime.tracker.data.database.AnimeDao
import com.anime.tracker.data.database.AnimeDatabase
import com.anime.tracker.data.repository.AnimeRepositoryImpl
import com.anime.tracker.domain.repository.AnimeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://graphql.anilist.co")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAniListApi(retrofit: Retrofit): AniListApiService {
        return retrofit.create(AniListApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnimeDatabase {
        return Room.databaseBuilder(
            context,
            AnimeDatabase::class.java,
            "anime_tracker_db"
        ).build()
    }

    @Provides
    fun provideAnimeDao(db: AnimeDatabase): AnimeDao = db.animeDao()

    @Provides
    @Singleton
    fun provideAnimeRepository(impl: AnimeRepositoryImpl): AnimeRepository = impl
}