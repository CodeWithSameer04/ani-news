package com.anime.tracker.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.SearchAnimeUseCase
import com.anime.tracker.domain.usecase.ToggleWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val list: List<Anime>) : SearchUiState
    data class Error(val msg: String) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchAnimeUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _savedIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedIds: StateFlow<Set<Int>> = _savedIds

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            searchUseCase(query)
                .onSuccess { _uiState.value = SearchUiState.Success(it) }
                .onFailure { _uiState.value = SearchUiState.Error(it.localizedMessage ?: "Unknown Error") }
        }
    }

    fun toggleWatchlist(anime: Anime) {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(anime)
            checkSavedStatus(anime.id)
        }
    }

    fun checkSavedStatus(id: Int) {
        viewModelScope.launch {
            val exists = toggleWatchlistUseCase.isAnimeSaved(id)
            _savedIds.value = if (exists) _savedIds.value + id else _savedIds.value - id
        }
    }
}