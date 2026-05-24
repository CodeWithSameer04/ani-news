package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    operator fun invoke(): Flow<List<Anime>> = repository.getWatchlist()
}
