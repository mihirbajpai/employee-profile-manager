package com.example.employeeprofile.platform

import androidx.compose.runtime.Composable

/**
 * A file the picker has copied into the app's own storage.
 *
 * The copy matters: what a picker hands back is usually a temporary grant that stops working
 * once the screen goes away, so nothing is stored in the database until there's a path the app
 * still owns tomorrow.
 */
data class PickedFile(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String
)

/** Where a profile photo can come from. */
enum class ImageSource { CAMERA, GALLERY }

/** Launches the platform pickers. Obtain one with [rememberMediaPicker]. */
interface MediaPicker {
    fun pickImage(source: ImageSource)
    fun pickDocument()
}

/**
 * The platform's picker, tied to the composition that owns it — Android needs an activity
 * result launcher registered during composition, which is why this is a composable rather than
 * something the Koin graph could hand out.
 *
 * [onError] reports anything the user should hear about, such as a file that's too large.
 */
@Composable
expect fun rememberMediaPicker(
    onImagePicked: (PickedFile) -> Unit,
    onDocumentPicked: (PickedFile) -> Unit,
    onError: (String) -> Unit
): MediaPicker
