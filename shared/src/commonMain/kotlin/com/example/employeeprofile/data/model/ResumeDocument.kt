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
) {
    companion object {
        /** The brief's ceiling for an uploaded resume. */
        const val MAX_BYTES = 5L * 1024 * 1024

        /** What the picker is allowed to offer: PDF, DOC and DOCX. */
        val ALLOWED_MIME_TYPES = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )

        /** Shown when a file is turned away, on both platforms. */
        const val TOO_LARGE_MESSAGE = "Resumes must be 5 MB or smaller"
    }
}
