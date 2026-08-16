package com.example.employeeprofile.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.employeeprofile.platform.documentsPath

/** iOS keeps the file in the app's Documents directory, which is backed up and app-private. */
fun employeeDatabaseBuilder(): RoomDatabase.Builder<EmployeeDatabase> =
    Room.databaseBuilder<EmployeeDatabase>(
        name = documentsPath() + "/" + EmployeeDatabase.FILE_NAME
    )
