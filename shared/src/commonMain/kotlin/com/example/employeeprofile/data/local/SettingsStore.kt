package com.example.employeeprofile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.employeeprofile.data.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/** The settings file's name; the directory it sits in is platform-specific. */
const val SETTINGS_FILE_NAME = "settings.preferences_pb"

private val THEME_KEY = stringPreferencesKey("theme_preference")

/**
 * Builds the store from a platform-supplied [path]. Only the location differs per platform —
 * the format and the reading and writing are shared.
 */
fun createSettingsDataStore(path: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path().toPath() })

/**
 * The app's saved preferences. Stored by enum name rather than ordinal, so reordering the enum
 * can't silently change what someone previously chose.
 */
class SettingsStore(private val dataStore: DataStore<Preferences>) {

    val themePreference: Flow<ThemePreference> = dataStore.data.map { preferences ->
        val stored = preferences[THEME_KEY]
        ThemePreference.entries.firstOrNull { it.name == stored } ?: ThemePreference.SYSTEM
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        dataStore.edit { it[THEME_KEY] = preference.name }
    }
}
