package com.example.employeeprofile.di

import androidx.room.RoomDatabase
import com.example.employeeprofile.data.local.EmployeeDatabase
import com.example.employeeprofile.data.local.employeeDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<EmployeeDatabase>> { employeeDatabaseBuilder(androidContext()) }
}
