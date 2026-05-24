package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

class SearchAnimeUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(query: String): Result<List<Anime>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchAnime(query)
    }
}
