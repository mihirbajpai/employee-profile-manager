package com.example.employeeprofile.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.employeeprofile.data.model.ResumeDocument
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTType
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
    private var cameraDelegate: CameraDelegate? = null
    private var documentDelegate: DocumentPickerDelegate? = null

    override fun pickImage(source: ImageSource) {
        when (source) {
            ImageSource.GALLERY -> presentPhotoPicker()
            ImageSource.CAMERA -> presentCamera()
        }
    }

    override fun pickDocument() {
        val types = RESUME_TYPE_IDENTIFIERS.mapNotNull(UTType::typeWithIdentifier)
        if (types.isEmpty()) {
            onError("Couldn't open the file picker")
            return
        }
        val delegate = DocumentPickerDelegate(onDocumentPicked, onError)
        documentDelegate = delegate
        val controller = UIDocumentPickerViewController(forOpeningContentTypes = types).apply {
            setDelegate(delegate)
            setAllowsMultipleSelection(false)
        }
        rootViewController()?.presentViewController(controller, animated = true, completion = null)
            ?: onError("Couldn't open the file picker")
    }

    /**
     * Recent simulators do offer a simulated camera, so this isn't a device-only path — but a
     * simulator that doesn't will report the source unavailable, and that's answered with a
     * message rather than a controller that comes up black. Needs NSCameraUsageDescription.
     */
    private fun presentCamera() {
        val cameraSource = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        if (UIImagePickerController.isSourceTypeAvailable(cameraSource).not()) {
            onError("This device has no camera")
            return
        }
        val delegate = CameraDelegate(onImagePicked, onError)
        cameraDelegate = delegate
        val controller = UIImagePickerController().apply {
            setSourceType(cameraSource)
            setDelegate(delegate)
        }
        rootViewController()?.presentViewController(controller, animated = true, completion = null)
            ?: onError("Couldn't open the camera")
    }

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

/** JPEG quality for a captured photo — plenty for an 80dp avatar, a fraction of the bytes. */
private const val CAPTURE_QUALITY = 0.9

@OptIn(ExperimentalForeignApi::class)
private class CameraDelegate(
    private val onPicked: (PickedFile) -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true, null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        if (image == null) {
            onError("Couldn't read that photo")
            return
        }
        val data = UIImageJPEGRepresentation(image, CAPTURE_QUALITY)
        if (data == null) {
            onError("Couldn't encode that photo")
            return
        }
        val saved = saveToAppStorage(data, "photo.jpg", mimeType = "image/jpeg")
        if (saved == null) onError("Couldn't save that photo") else onPicked(saved)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
    }
}

/** PDF, DOC and DOCX, as uniform type identifiers rather than MIME types. */
private val RESUME_TYPE_IDENTIFIERS = listOf(
    "com.adobe.pdf",
    "com.microsoft.word.doc",
    "org.openxmlformats.wordprocessingml.document"
)

@OptIn(ExperimentalForeignApi::class)
private class DocumentPickerDelegate(
    private val onPicked: (PickedFile) -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        readPickedDocument(url, onPicked, onError)
    }
}

/**
 * Reads a picked document and files it away, or explains why it couldn't.
 *
 * Separate from the delegate so it can be tested: UIKit only hands us the URL, and everything
 * that can go wrong afterwards — an unreadable file, one over the limit, a failed copy —
 * happens in here.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun readPickedDocument(
    url: NSURL,
    onPicked: (PickedFile) -> Unit,
    onError: (String) -> Unit
) {
    // A picked file arrives security-scoped: readable only between these two calls.
    val scoped = url.startAccessingSecurityScopedResource()
    val data = NSData.dataWithContentsOfURL(url)
    if (scoped) url.stopAccessingSecurityScopedResource()

    if (data == null) {
        onError("Couldn't read that file")
        return
    }
    if (data.length.toLong() > ResumeDocument.MAX_BYTES) {
        onError(ResumeDocument.TOO_LARGE_MESSAGE)
        return
    }
    val name = url.lastPathComponent ?: "resume"
    val saved = saveToAppStorage(data, name, mimeType = mimeTypeFor(name))
    if (saved == null) onError("Couldn't save that file") else onPicked(saved)
}

/** Enough of a mapping for the three types the picker offers. */
internal fun mimeTypeFor(name: String): String = when {
    name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
    name.endsWith(".docx", ignoreCase = true) ->
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    name.endsWith(".doc", ignoreCase = true) -> "application/msword"
    else -> ""
}
