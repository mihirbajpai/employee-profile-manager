package com.example.employeeprofile.platform

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Copies of files the user picked. */
internal const val MEDIA_DIRECTORY = "media"

/** Documents the app generates, kept apart from what the user brought in. */
internal const val EXPORT_DIRECTORY = "exports"

/** Must match the authority declared for the provider in the manifest. */
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

/**
 * A file inside one of the app's own directories, creating the directory if it's the first
 * time. [unique] stamps the name so two files picked with the same name don't collide — off for
 * generated documents, where overwriting the previous export is the point.
 */
internal fun Context.appFile(directory: String, name: String, unique: Boolean = true): File {
    val parent = File(filesDir, directory).apply { mkdirs() }
    return File(parent, if (unique) "${System.currentTimeMillis()}-$name" else name)
}

/** A URI another app can read, granted through the provider declared in the manifest. */
internal fun Context.sharableUri(file: File): Uri =
    FileProvider.getUriForFile(this, packageName + FILE_PROVIDER_SUFFIX, file)
