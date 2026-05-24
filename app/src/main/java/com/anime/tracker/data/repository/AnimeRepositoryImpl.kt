package com.anime.tracker.data.repository

import com.anime.tracker.data.api.AniListApiService
import com.anime.tracker.data.api.AniListQueries
import com.anime.tracker.data.api.GraphQLQuery
import com.anime.tracker.data.database.AnimeDao
import com.anime.tracker.data.database.AnimeEntity
import com.anime.tracker.data.database.CategoryEntity
import com.anime.tracker.domain.model.AiringEpisode
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.model.AnimeDetails
import com.anime.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val apiService: AniListApiService,
    private val dao: AnimeDao
) : AnimeRepository {

    override suspend fun searchAnime(query: String): Result<List<Anime>> {
        return runCatching {
            val payload = GraphQLQuery(
                query = AniListQueries.SEARCH_AIRING_QUERY,
                variables = mapOf("search" to query)
            )
            val response = apiService.postGraphQL(payload)
            response.data?.Page?.media?.map { dto ->
                Anime(
                    id = dto.id,
                    title = dto.title?.english ?: dto.title?.romaji ?: "Unknown Title",
                    imageUrl = dto.coverImage?.extraLarge ?: "",
                    status = dto.status ?: "UNKNOWN",
                    nextEpisode = dto.nextAiringEpisode?.episode,
                    airingAt = dto.nextAiringEpisode?.airingAt
                )
            } ?: emptyList()
        }
    }

    override fun getWatchlist(): Flow<List<Anime>> {
        return dao.getAllWatchlist().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertWatchlist(anime: Anime) {
        dao.insertAnime(AnimeEntity.fromDomain(anime))
    }

    override suspend fun removeFromWatchlist(id: Int) {
        dao.deleteById(id)
    }

    override suspend fun isPinned(id: Int): Boolean = dao.exists(id)

    override suspend fun syncWatchlistData(): Result<Unit> {
        return runCatching {
            val localItems = dao.getAllWatchlistStatic()
            if (localItems.isEmpty()) return Result.success(Unit)

            val ids = localItems.map { it.id }
            val payload = GraphQLQuery(
                query = AniListQueries.BATCH_SYNC_QUERY,
                variables = mapOf("ids" to ids)
            )
            val response = apiService.postGraphQL(payload)
            val categoryMap = localItems.associate { it.id to it.categoryName }
            
            response.data?.Page?.media?.forEach { dto ->
                val title = dto.title?.english ?: dto.title?.romaji ?: "Unknown Title"
                dao.insertAnime(
                    AnimeEntity(
                        id = dto.id,
                        title = title,
                        imageUrl = dto.coverImage?.extraLarge ?: "",
                        status = dto.status ?: "UNKNOWN",
                        nextEpisode = dto.nextAiringEpisode?.episode,
                        airingAt = dto.nextAiringEpisode?.airingAt,
                        categoryName = categoryMap[dto.id]
                    )
                )
            }
        }
    }

    override suspend fun getTrendingAnime(): Result<List<Anime>> {
        return runCatching {
            val payload = GraphQLQuery(
                query = AniListQueries.TRENDING_ANIME_QUERY,
                variables = emptyMap()
            )
            val response = apiService.postGraphQL(payload)
            response.data?.Page?.media?.map { dto ->
                Anime(
                    id = dto.id,
                    title = dto.title?.english ?: dto.title?.romaji ?: "Unknown Title",
                    imageUrl = dto.coverImage?.extraLarge ?: "",
                    status = dto.status ?: "UNKNOWN",
                    nextEpisode = dto.nextAiringEpisode?.episode,
                    airingAt = dto.nextAiringEpisode?.airingAt
                )
            } ?: emptyList()
        }
    }

    override suspend fun getAnimeDetails(id: Int): Result<AnimeDetails> {
        return runCatching {
            val payload = GraphQLQuery(
                query = AniListQueries.ANIME_DETAILS_QUERY,
                variables = mapOf("id" to id)
            )
            val response = apiService.postGraphQL(payload)
            val dto = response.data?.Media ?: throw Exception("Media not found")
            
            AnimeDetails(
                anime = Anime(
                    id = dto.id,
                    title = dto.title?.english ?: dto.title?.romaji ?: "Unknown Title",
                    imageUrl = dto.coverImage?.extraLarge ?: "",
                    status = dto.status ?: "UNKNOWN",
                    nextEpisode = dto.nextAiringEpisode?.episode,
                    airingAt = dto.nextAiringEpisode?.airingAt
                ),
                bannerImage = dto.bannerImage,
                description = dto.description,
                schedule = dto.airingSchedule?.nodes?.map { 
                    AiringEpisode(it.airingAt ?: 0L, it.episode ?: 0)
                } ?: emptyList()
            )
        }
    }

    override fun getCategories(): Flow<List<String>> {
        return dao.getAllCategories().map { list -> list.map { it.categoryName } }
    }

    override suspend fun addCategory(name: String) {
        dao.insertCategory(CategoryEntity(name))
    }

    override suspend fun updateAnimeCategory(id: Int, categoryName: String?) {
        dao.updateAnimeCategory(id, categoryName ?: "Default")
    }
}
