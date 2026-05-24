package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

class UpdateAnimeCategoryUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(id: Int, categoryName: String?) = 
        repository.updateAnimeCategory(id, categoryName)
}
