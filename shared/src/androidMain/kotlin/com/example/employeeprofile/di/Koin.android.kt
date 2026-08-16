package com.example.employeeprofile.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import com.example.employeeprofile.data.local.EmployeeDatabase
import com.example.employeeprofile.data.local.SETTINGS_FILE_NAME
import com.example.employeeprofile.data.local.createSettingsDataStore
import com.example.employeeprofile.data.local.employeeDatabaseBuilder
import java.io.File
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<EmployeeDatabase>> { employeeDatabaseBuilder(androidContext()) }
    single<DataStore<Preferences>> {
        val context = androidContext()
        createSettingsDataStore { File(context.filesDir, SETTINGS_FILE_NAME).absolutePath }
    }
}
