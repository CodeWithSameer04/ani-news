package com.anime.tracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anime.tracker.domain.usecase.GetWatchlistUseCase
import com.anime.tracker.domain.usecase.SyncAiringDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AiringSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUseCase: SyncAiringDataUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val syncResult = syncUseCase()
        if (syncResult.isFailure) return Result.retry()

        // Re-calculate downstream notifications following mutations to baseline metadata
        val watchlist = getWatchlistUseCase().first()
        watchlist.forEach { anime ->
            NotificationScheduler.scheduleRemindersForAnime(context, anime)
        }

        return Result.success()
    }
}
