package com.example.employeeprofile.data.model

/**
 * The uploaded resume. [path] points at a file the app copied into its own storage, so it stays
 * readable after the picker's temporary access is gone.
 */
data class ResumeDocument(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String
)
