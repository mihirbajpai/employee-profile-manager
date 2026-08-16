package com.example.employeeprofile

import android.app.Application
import com.example.employeeprofile.di.initKoin
import org.koin.android.ext.koin.androidContext

class EmployeeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@EmployeeApplication)
        }
    }
}
