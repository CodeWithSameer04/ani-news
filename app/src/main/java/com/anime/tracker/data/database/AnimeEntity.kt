package com.anime.tracker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anime.tracker.domain.model.Anime

@Entity(tableName = "watchlist_table")
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String,
    val status: String,
    val nextEpisode: Int?,
    val airingAt: Long?,
    val categoryName: String?
) {
    fun toDomain(): Anime = Anime(id, title, imageUrl, status, nextEpisode, airingAt, categoryName)

    companion object {
        fun fromDomain(domain: Anime): AnimeEntity = AnimeEntity(
            id = domain.id,
            title = domain.title,
            imageUrl = domain.imageUrl,
            status = domain.status,
            nextEpisode = domain.nextEpisode,
            airingAt = domain.airingAt,
            categoryName = domain.categoryName ?: "Default"
        )
    }
}
