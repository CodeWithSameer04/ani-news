package com.anime.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anime.tracker.presentation.navigation.Screen
import com.anime.tracker.presentation.library.LibraryScreen
import com.anime.tracker.presentation.library.LibraryViewModel

import com.anime.tracker.presentation.browse.BrowseScreen
import com.anime.tracker.presentation.browse.BrowseViewModel
import com.anime.tracker.presentation.detail.AnimeDetailScreen
import com.anime.tracker.presentation.detail.AnimeDetailViewModel
import com.anime.tracker.presentation.updates.UpdatesScreen
import com.anime.tracker.presentation.updates.UpdatesViewModel
import com.anime.tracker.presentation.settings.SettingsScreen
import com.anime.tracker.presentation.settings.SettingsViewModel
import com.anime.tracker.presentation.more.MoreScreen
import com.anime.tracker.presentation.more.MoreViewModel
import com.anime.tracker.presentation.search.SearchScreen
import com.anime.tracker.presentation.search.SearchViewModel
import com.anime.tracker.presentation.theme.AnimeTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val libraryViewModel: LibraryViewModel by viewModels()
    private val browseViewModel: BrowseViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val updatesViewModel: UpdatesViewModel by viewModels()
    private val moreViewModel: MoreViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeModeState = settingsViewModel.themeMode.collectAsState()
            val darkTheme = when (themeModeState.value) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            // Android 13+ Notification Permission Request
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    // Handle denial - maybe disable notifications in settings
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            AnimeTrackerTheme(darkTheme = darkTheme) {
                MainShell(
                    libraryViewModel = libraryViewModel,
                    browseViewModel = browseViewModel,
                    searchViewModel = searchViewModel,
                    updatesViewModel = updatesViewModel,
                    moreViewModel = moreViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun MainShell(
    libraryViewModel: LibraryViewModel,
    browseViewModel: BrowseViewModel,
    searchViewModel: SearchViewModel,
    updatesViewModel: UpdatesViewModel,
    moreViewModel: MoreViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Library to Icons.Default.CollectionsBookmark,
        Screen.Updates to Icons.Default.History,
        Screen.Browse to Icons.Default.Public,
        Screen.More to Icons.Default.MoreHoriz
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { (screen, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.Details.createRoute(id)) }
                )
            }
            composable(Screen.Updates.route) { 
                UpdatesScreen(viewModel = updatesViewModel) 
            }
            composable(Screen.Browse.route) { 
                BrowseScreen(
                    viewModel = browseViewModel,
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.Details.createRoute(id)) }
                ) 
            }
            composable(Screen.More.route) { 
                MoreScreen(
                    viewModel = moreViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                ) 
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToWatchlist = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("animeId") { type = NavType.IntType })
            ) {
                val detailViewModel: AnimeDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                AnimeDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text("$name Screen Coming Soon")
    }
}
