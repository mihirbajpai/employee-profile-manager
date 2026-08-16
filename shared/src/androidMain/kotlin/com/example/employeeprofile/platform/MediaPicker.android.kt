package com.example.employeeprofile.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/** Where copies of picked files live, inside the app's private storage. */
private const val MEDIA_DIRECTORY = "media"

/** Must match the authority declared for the provider in the manifest. */
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

@Composable
actual fun rememberMediaPicker(
    onImagePicked: (PickedFile) -> Unit,
    onDocumentPicked: (PickedFile) -> Unit,
    onError: (String) -> Unit
): MediaPicker {
    val context = LocalContext.current

    // Where the camera app is writing right now. Held across the launch, since the result
    // callback is only told whether it succeeded, not where the photo went.
    var captureTarget by remember { mutableStateOf<File?>(null) }

    // The system photo picker needs no runtime permission — it runs out of process and hands
    // back only what the user chose.
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        context.copyToAppStorage(uri, onError)?.let(onImagePicked)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val target = captureTarget
        captureTarget = null
        if (saved.not() || target == null) return@rememberLauncherForActivityResult
        onImagePicked(
            PickedFile(
                path = target.absolutePath,
                name = target.name,
                sizeBytes = target.length(),
                mimeType = "image/jpeg"
            )
        )
    }

    fun startCapture() {
        val target = context.newMediaFile("photo.jpg")
        captureTarget = target
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + FILE_PROVIDER_SUFFIX,
            target
        )
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCapture() else onError("Camera permission is needed to take a photo")
    }

    return remember(galleryLauncher, cameraLauncher, cameraPermissionLauncher) {
        object : MediaPicker {
            override fun pickImage(source: ImageSource) {
                when (source) {
                    ImageSource.GALLERY -> galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                    ImageSource.CAMERA -> {
                        if (context.hasCameraPermission()) {
                            startCapture()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            }

            override fun pickDocument() = onError("Document picking isn't wired up yet")
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

/** A fresh file under the app's media directory; the timestamp keeps names from colliding. */
private fun Context.newMediaFile(name: String): File {
    val directory = File(filesDir, MEDIA_DIRECTORY).apply { mkdirs() }
    return File(directory, "${System.currentTimeMillis()}-$name")
}

/**
 * Copies what the picker returned into the app's own files, so the path keeps working after
 * the temporary read grant expires.
 */
internal fun Context.copyToAppStorage(uri: Uri, onError: (String) -> Unit): PickedFile? {
    val metadata = queryMetadata(uri)
    val target = newMediaFile(metadata.name)
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
