package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

class GetTrendingAnimeUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(): Result<List<Anime>> = repository.getTrendingAnime()
}
