package com.anime.tracker.presentation.watchlist

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.*
import com.anime.tracker.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
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

    fun updateCategory(id: Int, categoryName: String?) {
        viewModelScope.launch {
            updateAnimeCategoryUseCase(id, categoryName)
        }
    }

    fun toggleWatchlist(anime: Anime) {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(anime)
        }
    }

    fun scheduleNotifications(context: Context, anime: Anime) {
        NotificationScheduler.scheduleRemindersForAnime(context, anime)
    }

    fun addEventToSystemCalendar(context: Context, anime: Anime) {
        val targetTimestamp = anime.airingAt ?: return
        val startTimeMs = targetTimestamp * 1000
        val endTimeMs = startTimeMs + (30 * 60 * 1000) // Default estimate duration to 30 mins

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "${anime.title} - Episode ${anime.nextEpisode ?: 1}")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMs)
            putExtra(CalendarContract.Events.DESCRIPTION, "Tracked execution via Anime Airing Tracker App.")
            putExtra(CalendarContract.Events.ALLOWED_REMINDERS, "METHOD_ALERT")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
