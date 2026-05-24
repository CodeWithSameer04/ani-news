package com.anime.tracker.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LibraryStats(
    val animeCount: Int = 0,
    val categoryCount: Int = 0
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    repository: AnimeRepository
) : ViewModel() {

    val stats: StateFlow<LibraryStats> = repository.getWatchlist()
        .map { watchlist ->
            // Count unique categories + total anime
            val uniqueCategories = watchlist.mapNotNull { it.categoryName }.distinct().size
            LibraryStats(
                animeCount = watchlist.size,
                categoryCount = uniqueCategories
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LibraryStats()
        )
}
