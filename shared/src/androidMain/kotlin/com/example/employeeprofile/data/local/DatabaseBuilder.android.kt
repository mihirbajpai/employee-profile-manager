package com.example.employeeprofile.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** Android keeps the file wherever the platform puts databases for this app. */
fun employeeDatabaseBuilder(context: Context): RoomDatabase.Builder<EmployeeDatabase> {
    val dbFile = context.getDatabasePath(EmployeeDatabase.FILE_NAME)
    return Room.databaseBuilder<EmployeeDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}
