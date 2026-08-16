package com.example.employeeprofile.data.model

/**
 * Skills offered in the form's checkbox group. Stored by [name], so entries may be renamed for
 * display but not reordered out of existence — a removed entry would orphan saved records.
 */
enum class Skill(val label: String) {
    KOTLIN("Kotlin"),
    SWIFT("Swift"),
    ANDROID("Android"),
    IOS("iOS"),
    BACKEND("Backend"),
    FRONTEND("Frontend"),
    DEVOPS("DevOps"),
    TESTING("Testing"),
    UI_DESIGN("UI Design"),
    DATA_ANALYSIS("Data Analysis"),
    PROJECT_MANAGEMENT("Project Management"),
    COMMUNICATION("Communication")
}
