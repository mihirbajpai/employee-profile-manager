package com.example.employeeprofile.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/** Where copies of picked files live, inside the app's Documents directory. */
private const val MEDIA_DIRECTORY = "media"

/**
 * Writes picked bytes into the app's own storage and describes the result.
 *
 * Pickers hand back a security-scoped URL that stops resolving once the picker goes away, so
 * the bytes are copied somewhere the app still owns before any path reaches the database.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun saveToAppStorage(data: NSData, name: String, mimeType: String): PickedFile? {
    val fileManager = NSFileManager.defaultManager
    val documents = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    ) ?: return null

    val mediaDirectory = documents.URLByAppendingPathComponent(MEDIA_DIRECTORY) ?: return null
    fileManager.createDirectoryAtURL(mediaDirectory, true, null, null)

    val target: NSURL = mediaDirectory.URLByAppendingPathComponent(uniqueName(name)) ?: return null
    if (data.writeToURL(target, atomically = true).not()) return null

    return PickedFile(
        path = target.path ?: return null,
        name = name,
        sizeBytes = data.length.toLong(),
        mimeType = mimeType
    )
}

/** Two files picked with the same name shouldn't overwrite each other. */
private fun uniqueName(name: String): String = "${nowMillis()}-$name"

/** The app's Documents directory, where the database and settings file live. */
@OptIn(ExperimentalForeignApi::class)
internal fun documentsPath(): String {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(documents?.path) { "Documents directory is unavailable" }
}

/** The controller to present a picker from. */
@Suppress("DEPRECATION")
internal fun rootViewController(): UIViewController? =
    UIApplication.sharedApplication.keyWindow?.rootViewController
