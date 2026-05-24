package com.anime.tracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AnimeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val animeId = intent.getIntExtra("anime_id", -1)
        val title = intent.getStringExtra("anime_title") ?: "Anime Alert"
        val episode = intent.getIntExtra("episode", 0)
        val airingAt = intent.getLongExtra("airing_at", 0L)

        if (animeId != -1) {
            showNotification(context, animeId, title, episode, airingAt)
        }
    }

    private fun showNotification(context: Context, id: Int, title: String, episode: Int, airingAt: Long) {
        val channelId = "anime_airing_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Anime Airing Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for new anime episodes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val message = if (episode > 0) "Episode $episode is airing now!" else "New episode is airing now!"
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            // Live countdown feature:
            // Passing the base time and enabling chronometer with countdown mode.
            .setWhen(airingAt * 1000)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)

        notificationManager.notify(id, builder.build())
    }
}
