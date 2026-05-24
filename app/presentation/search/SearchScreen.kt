package com.anime.tracker.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anime.tracker.domain.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToWatchlist: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Anime") },
                actions = {
                    IconButton(onClick = onNavigateToWatchlist) {
                        Icon(Icons.Default.Check, contentDescription = "Watchlist View")
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
                    query = it
                    viewModel.search(it)
                },
                label = { Text("Search title...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is SearchUiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Search for active releases") }
                is SearchUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is SearchUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.msg}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.search(query) }) { Text("Retry") }
                    }
                }
                is SearchUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.list, key = { it.id }) { item ->
                            LaunchedEffect(item.id) { viewModel.checkSavedStatus(item.id) }
                            SearchItem(
                                anime = item,
                                isSaved = savedIds.contains(item.id),
                                onToggle = { viewModel.toggleWatchlist(item) }
                            )
                        }
                    }
                }
            }
        }
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
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    }
}