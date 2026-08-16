package com.example.employeeprofile.platform

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

/** Where copies of picked files live, inside the app's private storage. */
private const val MEDIA_DIRECTORY = "media"

@Composable
actual fun rememberMediaPicker(
    onImagePicked: (PickedFile) -> Unit,
    onDocumentPicked: (PickedFile) -> Unit,
    onError: (String) -> Unit
): MediaPicker {
    val context = LocalContext.current

    // The system photo picker needs no runtime permission — it runs out of process and only
    // hands back what the user chose. Camera capture does, and arrives in its own commit.
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val copied = context.copyToAppStorage(uri, onError) ?: return@rememberLauncherForActivityResult
        onImagePicked(copied)
    }

    return remember(galleryLauncher) {
        object : MediaPicker {
            override fun pickImage(source: ImageSource) {
                when (source) {
                    ImageSource.GALLERY -> galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                    ImageSource.CAMERA -> onError("Camera isn't wired up yet")
                }
            }

            override fun pickDocument() = onError("Document picking isn't wired up yet")
        }
    }
}

/**
 * Copies what the picker returned into the app's own files, so the path keeps working after
 * the temporary read grant expires.
 */
internal fun Context.copyToAppStorage(uri: Uri, onError: (String) -> Unit): PickedFile? {
    val metadata = queryMetadata(uri)
    val directory = File(filesDir, MEDIA_DIRECTORY).apply { mkdirs() }
    val target = File(directory, "${System.currentTimeMillis()}-${metadata.name}")
    return try {
        contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use(input::copyTo)
        } ?: return null
        PickedFile(
            path = target.absolutePath,
            name = metadata.name,
            sizeBytes = target.length(),
            mimeType = contentResolver.getType(uri).orEmpty()
        )
    } catch (e: Exception) {
        onError(e.message ?: "Couldn't read that file")
        null
    }
}

private class UriMetadata(val name: String, val size: Long)

/** Display name and size as the content provider reports them, with a usable fallback. */
private fun Context.queryMetadata(uri: Uri): UriMetadata {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            return UriMetadata(
                name = if (nameIndex >= 0) cursor.getString(nameIndex) else "file",
                size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
            )
        }
    }
    return UriMetadata(name = uri.lastPathSegment ?: "file", size = 0L)
}
