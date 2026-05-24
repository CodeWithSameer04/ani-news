package com.anime.tracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Query("SELECT * FROM watchlist_table ORDER BY airingAt ASC")
    fun getAllWatchlist(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM watchlist_table")
    suspend fun getAllWatchlistStatic(): List<AnimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Query("DELETE FROM watchlist_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_table WHERE id = :id)")
    suspend fun exists(id: Int): Boolean
}