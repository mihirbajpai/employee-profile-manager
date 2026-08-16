package com.example.employeeprofile.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** iOS keeps the file in the app's Documents directory, which is backed up and app-private. */
@OptIn(ExperimentalForeignApi::class)
fun employeeDatabaseBuilder(): RoomDatabase.Builder<EmployeeDatabase> {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val path = requireNotNull(documents?.path) { "Documents directory is unavailable" }
    return Room.databaseBuilder<EmployeeDatabase>(name = "$path/${EmployeeDatabase.FILE_NAME}")
}
