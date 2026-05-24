package com.anime.tracker.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val updateAnimeCategoryUseCase: UpdateAnimeCategoryUseCase
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    val categories = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist = getWatchlistUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedIds = watchlist.map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun addCategory(name: String) {
        viewModelScope.launch { addCategoryUseCase(name) }
    }

    fun saveAnime(anime: Anime, category: String?) {
        viewModelScope.launch {
            // Ensure it's in watchlist first
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
