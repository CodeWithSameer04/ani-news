package com.anime.tracker.domain.repository

import com.anime.tracker.domain.model.Anime
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    suspend fun searchAnime(query: String): Result<List<Anime>>
    fun getWatchlist(): Flow<List<Anime>>
    suspend fun insertWatchlist(anime: Anime)
    suspend fun removeFromWatchlist(id: Int)
    suspend fun isPinned(id: Int): Boolean
    suspend fun syncWatchlistData(): Result<Unit>
}