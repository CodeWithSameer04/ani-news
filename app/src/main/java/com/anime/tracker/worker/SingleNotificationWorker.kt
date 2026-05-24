package com.anime.tracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anime.tracker.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SingleNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val animeId = inputData.getInt("anime_id", 0)
        val title = inputData.getString("anime_title") ?: "Tracked Anime"
        val episode = inputData.getInt("episode", 0)
        val timeKey = inputData.getString("time_key") ?: "now"

        val label = when (timeKey) {
            "3d" -> "Airing in 3 days!"
            "1d" -> "Airing in 1 day!"
            "8h" -> "Airing in 8 hours!"
            "1h" -> "Airing in 1 hour!"
            else -> "Is airing now!"
        }

        notificationHelper.showNotification(
            id = animeId + timeKey.hashCode(),
            title = title,
            message = "Episode $episode $label"
        )
        return Result.success()
    }
}
