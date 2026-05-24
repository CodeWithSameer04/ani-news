package com.anime.tracker.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BrowseUiState {
    object Loading : BrowseUiState
    data class Success(val list: List<Anime>) : BrowseUiState
    data class Error(val msg: String) : BrowseUiState
}

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getTrendingAnimeUseCase: GetTrendingAnimeUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val updateAnimeCategoryUseCase: UpdateAnimeCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var allTrending: List<Anime> = emptyList()

    val categories = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedIds = getWatchlistUseCase()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        fetchTrending()
    }

    fun refresh() {
        _isRefreshing.value = true
        if (allTrending.isNotEmpty()) {
            _uiState.value = BrowseUiState.Success(allTrending.shuffled())
            _isRefreshing.value = false
        } else {
            fetchTrending()
        }
    }

    private fun fetchTrending() {
        viewModelScope.launch {
            _uiState.value = BrowseUiState.Loading
            getTrendingAnimeUseCase()
                .onSuccess { 
                    allTrending = it
                    _uiState.value = BrowseUiState.Success(it.shuffled()) 
                }
                .onFailure { 
                    _uiState.value = BrowseUiState.Error(it.message ?: "Unknown Error") 
                }
            _isRefreshing.value = false
        }
    }

    fun saveAnimeWithCategory(anime: Anime, category: String?) {
        viewModelScope.launch {
            if (!toggleWatchlistUseCase.isAnimeSaved(anime.id)) {
                toggleWatchlistUseCase.execute(anime)
            }
            updateAnimeCategoryUseCase(anime.id, category ?: "Default")
        }
    }

    fun removeFromWatchlist(id: Int) {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(Anime(id, "", "", "", null, null))
        }
    }
}
