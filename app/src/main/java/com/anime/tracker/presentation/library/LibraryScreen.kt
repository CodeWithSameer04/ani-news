package com.anime.tracker.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anime.tracker.domain.model.Anime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var animeToSave by remember { mutableStateOf<Anime?>(null) }

    val dynamicTabs = remember(categories, watchlist) {
        val hasDefault = watchlist.any { it.categoryName == "Default" || it.categoryName == null }
        val customCategories = categories.filter { it != "Default" }
        
        if (hasDefault) {
            listOf("Default") + customCategories
        } else {
            customCategories
        }
    }

    val pagerState = rememberPagerState(pageCount = { dynamicTabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Library", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Category")
                        }
                    }
                )
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 16.dp,
                    divider = {},
                    containerColor = MaterialTheme.colorScheme.surface,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    dynamicTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { pageIndex ->
            val catName = dynamicTabs.getOrNull(pageIndex)
            val content = if (catName == "Default") {
                watchlist.filter { it.categoryName == "Default" || it.categoryName == null }
            } else {
                watchlist.filter { it.categoryName == catName }
            }

            if (content.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Library is empty", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(content, key = { it.id }) { anime ->
                        TachiyomiCard(
                            anime = anime,
                            isSaved = savedIds.contains(anime.id),
                            onLongClick = { animeToSave = anime },
                            onClick = {
                                if (!savedIds.contains(anime.id)) {
                                    animeToSave = anime
                                } else {
                                    onNavigateToDetail(anime.id)
                                }
                            }
                        )
                    }
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
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCatName.isNotBlank()) {
                        viewModel.addCategory(newCatName)
                        showAddCategoryDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (animeToSave != null) {
        val anime = animeToSave!!
        val isSaved = savedIds.contains(anime.id)
        val currentCategory = watchlist.find { it.id == anime.id }?.categoryName
        
        com.anime.tracker.presentation.detail.CategorySelectionDialog(
            categories = categories,
            currentCategory = currentCategory,
            isSaved = isSaved,
            onDismiss = { animeToSave = null },
            onSave = { category ->
                viewModel.saveAnime(anime, category)
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
fun TachiyomiCard(
    anime: Anime,
    isSaved: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(2f / 3f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = anime.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            // Episode Badge
            if (anime.nextEpisode != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Ep ${anime.nextEpisode}",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Title Over Image
            Text(
                text = anime.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
            
            if (isSaved) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                )
            }
        }
    }
}
