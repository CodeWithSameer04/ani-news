package com.anime.tracker.data.repository

import com.anime.tracker.data.api.AniListApiService
import com.anime.tracker.data.api.AniListQueries
import com.anime.tracker.data.api.GraphQLQuery
import com.anime.tracker.data.database.AnimeDao
import com.anime.tracker.data.database.AnimeEntity
import com.anime.tracker.domain.model.Anime
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
            response.data?.Page?.media?.forEach { dto ->
                val title = dto.title?.english ?: dto.title?.romaji ?: "Unknown Title"
                dao.insertAnime(
                    AnimeEntity(
                        id = dto.id,
                        title = title,
                        imageUrl = dto.coverImage?.extraLarge ?: "",
                        status = dto.status ?: "UNKNOWN",
                        nextEpisode = dto.nextAiringEpisode?.episode,
                        airingAt = dto.nextAiringEpisode?.airingAt
                    )
                )
            }
        }
    }
}