package com.example.employeeprofile.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
}

/**
 * Swift entry point — see iOSApp.swift. Kotlin default arguments aren't exported, and an
 * `init` prefix would come out of the Objective-C export as `doInit…`.
 */
fun startKoinForIos() {
    initKoin()
}
