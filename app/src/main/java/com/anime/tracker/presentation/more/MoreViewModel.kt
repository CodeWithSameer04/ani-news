package com.anime.tracker.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryStats(
    val animeCount: Int = 0,
    val categoryCount: Int = 0,
    val statusCounts: Map<String, Int> = emptyMap()
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    val categories: StateFlow<List<String>> = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<LibraryStats> = repository.getWatchlist()
        .map { watchlist ->
            val uniqueCategories = watchlist.mapNotNull { it.categoryName }.distinct().size
            val statusCounts = watchlist.groupBy { it.status }.mapValues { it.value.size }
            LibraryStats(
                animeCount = watchlist.size,
                categoryCount = uniqueCategories,
                statusCounts = statusCounts
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LibraryStats()
        )

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name)
        }
    }
}
