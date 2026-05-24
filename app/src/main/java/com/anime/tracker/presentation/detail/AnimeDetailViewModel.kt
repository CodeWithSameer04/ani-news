package com.anime.tracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.model.AnimeDetails
import com.anime.tracker.domain.repository.AnimeRepository
import com.anime.tracker.domain.usecase.GetCategoriesUseCase
import com.anime.tracker.domain.usecase.GetWatchlistUseCase
import com.anime.tracker.domain.usecase.ToggleWatchlistUseCase
import com.anime.tracker.domain.usecase.UpdateAnimeCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val details: AnimeDetails) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val updateAnimeCategoryUseCase: UpdateAnimeCategoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val categories = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedAnime = getWatchlistUseCase()
        .map { list -> list.find { it.id == animeId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        fetchDetails()
    }

    private fun fetchDetails() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            repository.getAnimeDetails(animeId)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { _uiState.value = DetailUiState.Error(it.message ?: "Unknown Error") }
        }
    }

    fun saveAnimeWithCategory(anime: Anime, category: String) {
        viewModelScope.launch {
            if (!toggleWatchlistUseCase.isAnimeSaved(anime.id)) {
                toggleWatchlistUseCase.execute(anime)
            }
            updateAnimeCategoryUseCase(anime.id, category)
        }
    }

    fun removeFromWatchlist() {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(Anime(animeId, "", "", "", null, null))
        }
    }
}
