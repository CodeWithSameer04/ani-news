package com.anime.tracker.presentation.detail

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anime.tracker.domain.model.AiringEpisode
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    viewModel: AnimeDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val savedAnime by viewModel.savedAnime.collectAsState()

    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    val isSaved = savedAnime != null
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Library",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is DetailUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message) }
            is DetailUiState.Success -> {
                val details = state.details
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            AsyncImage(
                                model = details.bannerImage ?: details.anime.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                            startY = 100f
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = details.anime.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (savedAnime != null) {
                                    TextButton(
                                        onClick = { showCategoryDialog = true },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "Category: ${savedAnime?.categoryName ?: "Default"}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (details.anime.airingAt != null && (details.anime.status == "RELEASING" || details.anime.status == "NOT_YET_RELEASED")) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Next Episode: ${details.anime.nextEpisode ?: "?"}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Airing in: ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    CountdownTimerDetailed(targetTimestampSec = details.anime.airingAt)
                                    Text(
                                        text = formatToLocalTimeDetailed(details.anime.airingAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Html.fromHtml(details.description ?: "", Html.FROM_HTML_MODE_COMPACT).toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Full Schedule",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    items(details.schedule.sortedByDescending { it.airingAt }) { episode ->
                        ScheduleItem(episode)
                    }
                }
            }
        }
    }
    if (showCategoryDialog && uiState is DetailUiState.Success) {
        val anime = (uiState as DetailUiState.Success).details.anime
        CategorySelectionDialog(
            categories = categories,
            currentCategory = savedAnime?.categoryName,
            isSaved = savedAnime != null,
            onDismiss = { showCategoryDialog = false },
            onSave = { category ->
                viewModel.saveAnimeWithCategory(anime, category)
                showCategoryDialog = false
            },
            onRemove = {
                viewModel.removeFromWatchlist()
                showCategoryDialog = false
            }
        )
    }
}

@Composable
fun CategorySelectionDialog(
    categories: List<String>,
    currentCategory: String?,
    isSaved: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onRemove: () -> Unit
) {
    var selectedCategory by remember { 
        mutableStateOf(if (currentCategory == "Default") null else currentCategory) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set categories", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                val customCategories = remember(categories) {
                    categories.filter { it != "Default" }
                }

                customCategories.forEach { category ->
                    CategorySelectionItem(
                        label = category,
                        isSelected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
                
                if (isSaved) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ListItem(
                        headlineContent = { Text("Remove from Watchlist", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { onRemove() },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(selectedCategory ?: "Default")
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { /* TODO: Open categories management */ }) {
                    Text("Edit")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun CategorySelectionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun ScheduleItem(episode: AiringEpisode) {
    ListItem(
        headlineContent = { Text("Episode ${episode.episode}", fontWeight = FontWeight.Medium) },
        supportingContent = { Text(formatToLocalTimeDetailed(episode.airingAt)) },
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "#${episode.episode}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

@Composable
fun CountdownTimerDetailed(targetTimestampSec: Long) {
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
        text = displayStr,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

fun calculateRemainingTime(targetTimestampSec: Long): Long {
    val currentSec = System.currentTimeMillis() / 1000
    return (targetTimestampSec - currentSec).coerceAtLeast(0)
}

fun formatToLocalTimeDetailed(timestampSec: Long): String {
    val instant = Instant.ofEpochSecond(timestampSec)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    // Friday, Oct 24 at 8:30 PM
    return localDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mm a"))
}
