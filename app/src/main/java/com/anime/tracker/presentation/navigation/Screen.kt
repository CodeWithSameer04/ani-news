package com.anime.tracker.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Library : Screen("library", "Library")
    object Updates : Screen("updates", "Updates")
    object Browse : Screen("browse", "Browse")
    object More : Screen("more", "More")
    object Search : Screen("search", "Search")
    object Settings : Screen("settings", "Settings")
    object DataStorage : Screen("data_storage", "Data and Storage")
    object Statistics : Screen("statistics", "Statistics")
    object Categories : Screen("categories", "Categories")
    object Details : Screen("details/{animeId}", "Details") {
        fun createRoute(animeId: Int) = "details/$animeId"
    }
}
