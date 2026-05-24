package com.anime.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anime.tracker.presentation.navigation.Screen
import com.anime.tracker.presentation.search.SearchScreen
import com.anime.tracker.presentation.search.SearchViewModel
import com.anime.tracker.presentation.theme.AnimeTrackerTheme
import com.anime.tracker.presentation.watchlist.WatchlistScreen
import com.anime.tracker.presentation.watchlist.WatchlistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val searchViewModel: SearchViewModel by viewModels()
    private val watchlistViewModel: WatchlistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimeTrackerTheme {
                NavigationComponent(
                    searchViewModel = searchViewModel,
                    watchlistViewModel = watchlistViewModel
                )
            }
        }
    }
}

@Composable
fun NavigationComponent(
    searchViewModel: SearchViewModel,
    watchlistViewModel: WatchlistViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Search.route) {
        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = searchViewModel,
                onNavigateToWatchlist = { navController.navigate(Screen.Watchlist.route) }
            )
        }
        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                viewModel = watchlistViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}