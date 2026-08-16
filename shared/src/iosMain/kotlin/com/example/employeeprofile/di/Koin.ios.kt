package com.example.employeeprofile.di

import androidx.room.RoomDatabase
import com.example.employeeprofile.data.local.EmployeeDatabase
import com.example.employeeprofile.data.local.employeeDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<EmployeeDatabase>> { employeeDatabaseBuilder() }
}

/**
 * Swift entry point — see iOSApp.swift. Kotlin default arguments aren't exported, and an
 * `init` prefix would come out of the Objective-C export as `doInit…`.
 */
fun startKoinForIos() {
    initKoin()
}
