package com.anime.tracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AnimeEntity::class, CategoryEntity::class], version = 2, exportSchema = false)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}
