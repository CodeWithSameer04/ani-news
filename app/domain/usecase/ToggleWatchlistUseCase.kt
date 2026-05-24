package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

class ToggleWatchlistUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend fun isAnimeSaved(id: Int): Boolean = repository.isPinned(id)

    suspend fun execute(anime: Anime) {
        if (repository.isPinned(anime.id)) {
            repository.removeFromWatchlist(anime.id)
        } else {
            repository.insertWatchlist(anime)
        }
    }
}