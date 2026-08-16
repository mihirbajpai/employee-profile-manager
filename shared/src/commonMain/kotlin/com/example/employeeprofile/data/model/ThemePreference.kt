package com.example.employeeprofile.data.model

/**
 * What the user asked the app to look like. [SYSTEM] defers to the device setting, which is the
 * default until they say otherwise.
 */
enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    /** The next option when the theme button is tapped: System → Light → Dark → System. */
    fun next(): ThemePreference = entries[(ordinal + 1) % entries.size]
}
