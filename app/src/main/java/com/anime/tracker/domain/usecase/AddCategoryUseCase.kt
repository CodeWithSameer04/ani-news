package com.anime.tracker.domain.usecase

import com.anime.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(name: String) = repository.addCategory(name)
}
