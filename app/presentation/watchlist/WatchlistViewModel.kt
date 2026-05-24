package com.anime.tracker.presentation.watchlist

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.domain.usecase.GetWatchlistUseCase
import com.anime.tracker.domain.usecase.ToggleWatchlistUseCase
import com.anime.tracker.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    getWatchlistUseCase: GetWatchlistUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {

    val watchlist: StateFlow<List<Anime>> = getWatchlistUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeItem(id: Int) {
        viewModelScope.launch {
            toggleWatchlistUseCase.execute(
                Anime(id, "", "", "", null, null)
            )
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