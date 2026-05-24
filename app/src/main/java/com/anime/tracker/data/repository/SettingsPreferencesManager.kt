package com.anime.tracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DEFAULT_START_SCREEN = stringPreferencesKey("default_start_screen")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { it[THEME_MODE] ?: 0 }
    val defaultStartScreen: Flow<String> = context.dataStore.data.map { it[DEFAULT_START_SCREEN] ?: "Library" }
    val enableNotifications: Flow<Boolean> = context.dataStore.data.map { it[ENABLE_NOTIFICATIONS] ?: true }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setDefaultStartScreen(screen: String) {
        context.dataStore.edit { it[DEFAULT_START_SCREEN] = screen }
    }

    suspend fun setEnableNotifications(enabled: Boolean) {
        context.dataStore.edit { it[ENABLE_NOTIFICATIONS] = enabled }
    }
}
