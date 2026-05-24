package com.anime.tracker.presentation.updates

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    viewModel: UpdatesViewModel
) {
    val updates by viewModel.updates.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Updates", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        if (updates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("No recent updates from your library", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(updates) { item ->
                    UpdateListRow(item)
                }
            }
        }
    }
}

@Composable
fun UpdateListRow(item: UpdateItem) {
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        item.airingAt * 1000,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    ListItem(
        headlineContent = { Text(item.anime.title, fontWeight = FontWeight.Bold, maxLines = 1) },
        supportingContent = {
            Column {
                Text("Episode ${item.episode}")
                Text(relativeTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        leadingContent = {
            AsyncImage(
                model = item.anime.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp, 75.dp),
                contentScale = ContentScale.Crop
            )
        }
    )
}
