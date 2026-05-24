package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

class SyncAiringDataUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.syncWatchlistData()
}
