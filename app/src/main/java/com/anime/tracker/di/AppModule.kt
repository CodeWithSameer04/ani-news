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
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.inject.Singleton

class RateLimitException(message: String) : IOException(message)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val rateLimitInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 429) {
                throw RateLimitException("Rate Limited by AniList. Please wait a moment.")
            }
            response
        }
        return OkHttpClient.Builder()
            .addInterceptor(rateLimitInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://graphql.anilist.co")
            .client(okHttpClient)
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
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAnimeDao(db: AnimeDatabase): AnimeDao = db.animeDao()

    @Provides
    @Singleton
    fun provideAnimeRepository(impl: AnimeRepositoryImpl): AnimeRepository = impl
}
