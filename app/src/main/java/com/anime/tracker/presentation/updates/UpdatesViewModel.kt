package com.anime.tracker.presentation.updates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.AiringEpisode
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class UpdateItem(
    val anime: Anime,
    val episode: Int,
    val airingAt: Long
)

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    repository: AnimeRepository
) : ViewModel() {

    val updates: StateFlow<List<UpdateItem>> = repository.getWatchlist()
        .map { watchlist ->
            watchlist.mapNotNull { anime ->
                if (anime.airingAt != null && anime.nextEpisode != null) {
                    UpdateItem(anime, anime.nextEpisode, anime.airingAt)
                } else null
            }.sortedByDescending { it.airingAt }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
