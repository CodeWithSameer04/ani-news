package com.anime.tracker.presentation.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.presentation.library.TachiyomiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    var animeToSave by remember { mutableStateOf<Anime?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            when (val state = uiState) {
                is BrowseUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BrowseUiState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.msg}", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                is BrowseUiState.Success -> {
                    if (state.list.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text("No trending anime found", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(state.list, key = { it.id }) { anime ->
                                TachiyomiCard(
                                    anime = anime,
                                    isSaved = savedIds.contains(anime.id),
                                    onLongClick = { animeToSave = anime },
                                    onClick = {
                                        if (savedIds.contains(anime.id)) {
                                            onNavigateToDetail(anime.id)
                                        } else {
                                            animeToSave = anime
                                        }
                                    }
                                )
                            }
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
            currentCategory = null, // In browse, we don't necessarily know the category without fetching it
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
