package com.anime.tracker.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val list: List<Anime>) : SearchUiState
    data class Error(val msg: String) : SearchUiState
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchAnimeUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val updateAnimeCategoryUseCase: UpdateAnimeCategoryUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _savedIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedIds: StateFlow<Set<Int>> = _savedIds

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categories = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    executeSearch(query)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private suspend fun executeSearch(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        _uiState.value = SearchUiState.Loading
        searchUseCase(query)
            .onSuccess { _uiState.value = SearchUiState.Success(it) }
            .onFailure { _uiState.value = SearchUiState.Error(it.localizedMessage ?: "Unknown Error") }
    }

    fun search(query: String) {
        viewModelScope.launch {
            executeSearch(query)
        }
    }

    fun saveAnimeWithCategory(anime: Anime, category: String?) {
        viewModelScope.launch {
            if (!toggleWatchlistUseCase.isAnimeSaved(anime.id)) {
                toggleWatchlistUseCase.execute(anime)
            }
            updateAnimeCategoryUseCase(anime.id, category ?: "Default")
            checkSavedStatus(anime.id)
        }
    }

    fun removeFromWatchlist(id: Int) {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(Anime(id, "", "", "", null, null))
            checkSavedStatus(id)
        }
    }

    fun checkSavedStatus(id: Int) {
        viewModelScope.launch {
            val exists = toggleWatchlistUseCase.isAnimeSaved(id)
            _savedIds.value = if (exists) _savedIds.value + id else _savedIds.value - id
        }
    }
}
