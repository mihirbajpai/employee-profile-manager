package com.example.employeeprofile.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** What the photo picker is asked to hand back. */
private const val IMAGE_TYPE_IDENTIFIER = "public.image"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberMediaPicker(
    onImagePicked: (PickedFile) -> Unit,
    onDocumentPicked: (PickedFile) -> Unit,
    onError: (String) -> Unit
): MediaPicker = remember(onImagePicked, onDocumentPicked, onError) {
    IosMediaPicker(onImagePicked, onDocumentPicked, onError)
}

/**
 * Holds the delegates for as long as the picker itself lives. UIKit keeps only a weak
 * reference to a delegate, so anything created inline for the call would be collected before
 * the user finished choosing.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosMediaPicker(
    private val onImagePicked: (PickedFile) -> Unit,
    private val onDocumentPicked: (PickedFile) -> Unit,
    private val onError: (String) -> Unit
) : MediaPicker {

    private var photoDelegate: PhotoPickerDelegate? = null

    override fun pickImage(source: ImageSource) {
        when (source) {
            ImageSource.GALLERY -> presentPhotoPicker()
            ImageSource.CAMERA -> onError("Camera isn't wired up yet")
        }
    }

    override fun pickDocument() = onError("Document picking isn't wired up yet")

    private fun presentPhotoPicker() {
        val configuration = PHPickerConfiguration().apply {
            setFilter(PHPickerFilter.imagesFilter())
            setSelectionLimit(1)
        }
        val delegate = PhotoPickerDelegate(onImagePicked, onError)
        photoDelegate = delegate
        val controller = PHPickerViewController(configuration).apply {
            setDelegate(delegate)
        }
        rootViewController()?.presentViewController(controller, animated = true, completion = null)
            ?: onError("Couldn't open the photo picker")
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PhotoPickerDelegate(
    private val onPicked: (PickedFile) -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
        val provider = result.itemProvider

        provider.loadDataRepresentationForTypeIdentifier(IMAGE_TYPE_IDENTIFIER) { data, error ->
            // The completion lands on a background queue; everything below touches UI state.
            dispatch_async(dispatch_get_main_queue()) {
                deliver(data, error, provider.suggestedName ?: "photo.jpg")
            }
        }
    }

    private fun deliver(data: NSData?, error: NSError?, suggestedName: String) {
        if (data == null) {
            onError(error?.localizedDescription ?: "Couldn't read that photo")
            return
        }
        val saved = saveToAppStorage(data, suggestedName, mimeType = "image/jpeg")
        if (saved == null) {
            onError("Couldn't save that photo")
            return
        }
        onPicked(saved)
    }
}
