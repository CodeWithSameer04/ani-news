package com.anime.tracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrendingAnimeUseCase: GetTrendingAnimeUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateAnimeCategoryUseCase: UpdateAnimeCategoryUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {

    private val _trendingAnime = MutableStateFlow<List<Anime>>(emptyList())
    val trendingAnime: StateFlow<List<Anime>> = _trendingAnime.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredWatchlist: StateFlow<List<Anime>> = combine(
        getWatchlistUseCase(),
        _selectedCategory
    ) { watchlist, selected ->
        if (selected == null) watchlist
        else watchlist.filter { it.categoryName == selected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedIds: StateFlow<Set<Int>> = getWatchlistUseCase()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        fetchTrending()
    }

    private fun fetchTrending() {
        viewModelScope.launch {
            getTrendingAnimeUseCase().onSuccess {
                _trendingAnime.value = it
            }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            addCategoryUseCase(name)
        }
    }

    fun updateAnimeCategory(id: Int, categoryName: String?) {
        viewModelScope.launch {
            updateAnimeCategoryUseCase(id, categoryName)
        }
    }

    fun addToWatchlist(anime: Anime) {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(anime)
        }
    }
}
