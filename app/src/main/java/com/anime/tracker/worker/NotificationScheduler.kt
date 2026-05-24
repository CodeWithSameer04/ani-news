package com.anime.tracker.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anime.tracker.domain.model.Anime

object NotificationScheduler {

    fun scheduleAiringNotification(context: Context, anime: Anime) {
        val airingAt = anime.airingAt ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Exact alarms on Android 12+ require permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If we can't schedule exact alarms, we could fall back to inexact,
                // but the prompt specified exact timing. 
                // We will attempt to schedule anyway as USE_EXACT_ALARM is in manifest.
            }
        }

        val intent = Intent(context, AnimeAlarmReceiver::class.java).apply {
            putExtra("anime_id", anime.id)
            putExtra("anime_title", anime.title)
            putExtra("episode", anime.nextEpisode ?: 0)
            putExtra("airing_at", airingAt)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            anime.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMs = airingAt * 1000

        // Only schedule if the time is in the future
        if (triggerTimeMs > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
        }
    }

    fun cancelNotification(context: Context, animeId: Int) {
        val intent = Intent(context, AnimeAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            animeId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
