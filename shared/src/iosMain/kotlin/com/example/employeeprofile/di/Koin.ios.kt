package com.example.employeeprofile.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import com.example.employeeprofile.data.local.EmployeeDatabase
import com.example.employeeprofile.data.local.SETTINGS_FILE_NAME
import com.example.employeeprofile.data.local.createSettingsDataStore
import com.example.employeeprofile.data.local.employeeDatabaseBuilder
import com.example.employeeprofile.platform.documentsPath
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<EmployeeDatabase>> { employeeDatabaseBuilder() }
    single<DataStore<Preferences>> {
        createSettingsDataStore { documentsPath() + "/" + SETTINGS_FILE_NAME }
    }
}

/**
 * Swift entry point — see iOSApp.swift. Kotlin default arguments aren't exported, and an
 * `init` prefix would come out of the Objective-C export as `doInit…`.
 */
fun startKoinForIos() {
    initKoin()
}
