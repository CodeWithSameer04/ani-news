package com.anime.tracker.presentation.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    onNavigateToStats: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text("Statistics") },
                supportingContent = { 
                    Text("Tracking ${stats.animeCount} anime across ${stats.categoryCount} categories") 
                },
                leadingContent = { Icon(Icons.Default.BarChart, contentDescription = null) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Categories") },
                supportingContent = { Text("Manage custom folders") },
                leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            ListItem(
                headlineContent = { Text("Data and storage") },
                supportingContent = { Text("Manage database and cache") },
                leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            ListItem(
                headlineContent = { Text("Settings") },
                supportingContent = { Text("App configuration and theme") },
                leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onNavigateToSettings() }
            )

            ListItem(
                headlineContent = { Text("About") },
                supportingContent = { Text("Version 1.0.0") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
