package com.anime.tracker.presentation.navigation

sealed class Screen(val route: String) {
    object Search : Screen("search_screen")
    object Watchlist : Screen("watchlist_screen")
}