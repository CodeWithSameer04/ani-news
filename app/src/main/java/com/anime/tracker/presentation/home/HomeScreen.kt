package com.anime.tracker.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anime.tracker.domain.model.Anime
import com.anime.tracker.presentation.watchlist.CountdownTimer
import com.anime.tracker.presentation.watchlist.formatToLocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: () -> Unit
) {
    val trendingRaw by viewModel.trendingAnime.collectAsState()
    val watchlist by viewModel.filteredWatchlist.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    // Exactly 8 trending cards (shuffled/trimmed)
    val trendingLimit = remember(trendingRaw) {
        trendingRaw.shuffled().take(8)
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val displayList = if (selectedCategory == null) {
        // Show 8 trending + Full watchlist if no category selected
        // Requirement says "it should display exactly 8 trending anime cards" when screen first opens.
        // If "Show All Trending" is clicked, we reset filter.
        // We'll combine trending and watchlist for the grid or prioritize watchlist?
        // Let's assume the grid shows the filtered list or the "Front Page" (trending + watchlist)
        trendingLimit + watchlist.filter { !trendingLimit.any { t -> t.id == it.id } }
    } else {
        watchlist
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AniNews Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Create Category") },
                                onClick = {
                                    showMenu = false
                                    showAddCategoryDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Show All Trending") },
                                onClick = {
                                    showMenu = false
                                    viewModel.selectCategory(null)
                                }
                            )
                            HorizontalDivider()
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.selectCategory(category)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (displayList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Nothing to show here.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayList, key = { it.id }) { anime ->
                    HomeGridCard(
                        anime = anime,
                        isSaved = savedIds.contains(anime.id),
                        categories = categories,
                        onAdd = { viewModel.addToWatchlist(anime) },
                        onUpdateCategory = { cat -> viewModel.updateAnimeCategory(anime.id, cat) }
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("New Category") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCatName.isNotBlank()) {
                        viewModel.addCategory(newCatName)
                        showAddCategoryDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HomeGridCard(
    anime: Anime,
    isSaved: Boolean,
    categories: List<String>,
    onAdd: () -> Unit,
    onUpdateCategory: (String?) -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = anime.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
                if (!isSaved) {
                    FilledIconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                if (anime.nextEpisode != null) {
                    Text(
                        text = "Episode ${anime.nextEpisode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (anime.airingAt != null) {
                    CountdownTimer(targetTimestampSec = anime.airingAt)
                    Text(
                        text = formatToLocalTime(anime.airingAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (isSaved) {
                    Box {
                        TextButton(
                            onClick = { showCategoryMenu = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = anime.categoryName ?: "Assign Folder",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    onUpdateCategory(null)
                                    showCategoryMenu = false
                                }
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        onUpdateCategory(category)
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
