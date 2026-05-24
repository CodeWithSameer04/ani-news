package com.anime.tracker.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val defaultStartScreen by viewModel.defaultStartScreen.collectAsState()
    val enableNotifications by viewModel.enableNotifications.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setEnableNotifications(true)
        } else {
            viewModel.setEnableNotifications(false)
        }
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showStartScreenDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { SettingHeader("Appearance") }
            item {
                val themeLabel = when (themeMode) {
                    1 -> "Light"
                    2 -> "Dark"
                    else -> "System Default"
                }
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text(themeLabel) },
                    modifier = Modifier.clickable { showThemeDialog = true }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { SettingHeader("General") }
            item {
                ListItem(
                    headlineContent = { Text("Start Screen") },
                    supportingContent = { Text(defaultStartScreen) },
                    modifier = Modifier.clickable { showStartScreenDialog = true }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { SettingHeader("Library & Notifications") }
            item {
                ListItem(
                    headlineContent = { Text("Enable Notifications") },
                    trailingContent = {
                        Switch(
                            checked = enableNotifications,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setEnableNotifications(checked)
                                }
                            }
                        )
                    }
                )
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeOption("System Default", 0, themeMode) { viewModel.setThemeMode(it); showThemeDialog = false }
                    ThemeOption("Light", 1, themeMode) { viewModel.setThemeMode(it); showThemeDialog = false }
                    ThemeOption("Dark", 2, themeMode) { viewModel.setThemeMode(it); showThemeDialog = false }
                }
            },
            confirmButton = {}
        )
    }

    if (showStartScreenDialog) {
        AlertDialog(
            onDismissRequest = { showStartScreenDialog = false },
            title = { Text("Select Start Screen") },
            text = {
                Column {
                    StartScreenOption("Library", defaultStartScreen) { viewModel.setDefaultStartScreen(it); showStartScreenDialog = false }
                    StartScreenOption("Updates", defaultStartScreen) { viewModel.setDefaultStartScreen(it); showStartScreenDialog = false }
                    StartScreenOption("Browse", defaultStartScreen) { viewModel.setDefaultStartScreen(it); showStartScreenDialog = false }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun SettingHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ThemeOption(label: String, mode: Int, currentMode: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = mode == currentMode, onClick = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label)
    }
}

@Composable
fun StartScreenOption(label: String, current: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(label) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = label == current, onClick = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label)
    }
}
