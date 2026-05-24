package com.anime.tracker.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.anime.tracker.domain.model.Anime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleRemindersForAnime(context: Context, anime: Anime) {
        val targetTimestampSec = anime.airingAt ?: return
        val currentMs = System.currentTimeMillis()
        val targetMs = targetTimestampSec * 1000

        // Intervals mapped in milliseconds prior to air time
        val intervals = mapOf(
            "24h" to TimeUnit.HOURS.toMillis(24),
            "8h" to TimeUnit.HOURS.toMillis(8),
            "1h" to TimeUnit.HOURS.toMillis(1),
            "now" to 0L
        )

        intervals.forEach { (key, offset) ->
            val triggerTimeMs = targetMs - offset
            val delayMs = triggerTimeMs - currentMs

            if (delayMs > 0) {
                val inputData = Data.Builder()
                    .putInt("anime_id", anime.id)
                    .putString("anime_title", anime.title)
                    .putInt("episode", anime.nextEpisode ?: 0)
                    .putString("time_key", key)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(SingleNotificationWorker::class.java)
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                // Unique tag allows simple adjustments without multiplying schedules
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "anime_notify_${anime.id}_$key",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }
}