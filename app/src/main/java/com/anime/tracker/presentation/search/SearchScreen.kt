package com.anime.tracker.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anime.tracker.domain.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToWatchlist: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var animeToSave by remember { mutableStateOf<Anime?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Anime") },
                actions = {
                    IconButton(onClick = onNavigateToWatchlist) {
                        Icon(Icons.Default.Star, contentDescription = "View Watchlist")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    viewModel.onSearchQueryChanged(it)
                },
                label = { Text("Search currently airing anime...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Type above to search seasonal releases!")
                    }
                }
                is SearchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SearchUiState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Oops! Network failure.", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.search(query) }) { Text("Retry") }
                        }
                    }
                }
                is SearchUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.list, key = { it.id }) { item ->
                            LaunchedEffect(item.id) { viewModel.checkSavedStatus(item.id) }
                            SearchItem(
                                anime = item,
                                isSaved = savedIds.contains(item.id),
                                onToggle = { animeToSave = item }
                            )
                        }
                    }
                }
            }
        }
    }

    if (animeToSave != null) {
        val anime = animeToSave!!
        val isSaved = savedIds.contains(anime.id)

        com.anime.tracker.presentation.detail.CategorySelectionDialog(
            categories = categories,
            currentCategory = null,
            isSaved = isSaved,
            onDismiss = { animeToSave = null },
            onSave = { category ->
                viewModel.saveAnimeWithCategory(anime, category)
                animeToSave = null
            },
            onRemove = {
                viewModel.removeFromWatchlist(anime.id)
                animeToSave = null
            }
        )
    }
}

@Composable
fun SearchItem(anime: Anime, isSaved: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = anime.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(70.dp, 100.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(anime.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text("Status: ${anime.status}", style = MaterialTheme.typography.bodySmall)
                if (anime.status.equals("RELEASING", ignoreCase = true) && (anime.nextEpisode ?: 1) > 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Recently Aired: Ep ${anime.nextEpisode!! - 1}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Star,
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    contentDescription = null
                )
            }
        }
    }
}