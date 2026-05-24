package com.anime.tracker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.tracker.data.repository.SettingsPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: SettingsPreferencesManager
) : ViewModel() {

    val themeMode: StateFlow<Int> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val defaultStartScreen: StateFlow<String> = preferencesManager.defaultStartScreen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Library")

    val enableNotifications: StateFlow<Boolean> = preferencesManager.enableNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setDefaultStartScreen(screen: String) {
        viewModelScope.launch {
            preferencesManager.setDefaultStartScreen(screen)
        }
    }

    fun setEnableNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setEnableNotifications(enabled)
        }
    }
}
