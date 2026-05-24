package com.anime.tracker.presentation.watchlist

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anime.tracker.domain.model.Anime
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onBack: () -> Unit
) {
    val list by viewModel.watchlist.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Watchlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Your watchlist is empty.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list, key = { it.id }) { item ->
                    WatchlistItem(
                        anime = item,
                        onRemove = { viewModel.removeItem(item.id) },
                        onCalendarExport = { viewModel.addEventToSystemCalendar(context, item) },
                        onInitNotifications = { viewModel.scheduleNotifications(context, item) }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistItem(
    anime: Anime,
    onRemove: () -> Unit,
    onCalendarExport: () -> Unit,
    onInitNotifications: () -> Unit
) {
    // Automatically initialize notification intervals upon card layout binding
    LaunchedEffect(anime.id) { onInitNotifications() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = anime.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(75.dp, 110.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(anime.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)

                if (anime.airingAt != null && anime.nextEpisode != null) {
                    Text("Episode: ${anime.nextEpisode}", style = MaterialTheme.typography.bodyMedium)
                    CountdownTimer(targetTimestampSec = anime.airingAt)
                    Text(
                        text = "Local Time: " + formatToLocalTime(anime.airingAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text("No upcoming scheduling found", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
                if (anime.airingAt != null) {
                    IconButton(onClick = onCalendarExport) {
                        Icon(Icons.Default.DateRange, contentDescription = "Add Calendar Event")
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTimer(targetTimestampSec: Long) {
    var timeLeft by remember { mutableStateOf(calculateRemainingTime(targetTimestampSec)) }

    LaunchedEffect(targetTimestampSec) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft = calculateRemainingTime(targetTimestampSec)
        }
    }

    val displayStr = if (timeLeft > 0) {
        val days = timeLeft / (24 * 3600)
        val hours = (timeLeft % (24 * 3600)) / 3600
        val minutes = (timeLeft % 3600) / 60
        val seconds = timeLeft % 60
        String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds)
    } else {
        "Aired"
    }

    Text(
        text = "Airing in: $displayStr",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

fun calculateRemainingTime(targetTimestampSec: Long): Long {
    val currentSec = System.currentTimeMillis() / 1000
    return (targetTimestampSec - currentSec).coerceAtLeast(0)
}

fun formatToLocalTime(timestampSec: Long): String {
    val instant = Instant.ofEpochSecond(timestampSec)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"))
}