package com.example.employeeprofile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.local.SettingsStore
import com.example.employeeprofile.data.model.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The app-wide preferences. Held at the root of the UI, since the theme it decides wraps every
 * screen rather than belonging to any one of them.
 */
class SettingsViewModel(private val settings: SettingsStore) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = settings.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = ThemePreference.SYSTEM
        )

    /** Steps to the next option and writes it away; the flow above brings it back. */
    fun onCycleTheme() {
        viewModelScope.launch {
            settings.setThemePreference(themePreference.value.next())
        }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
