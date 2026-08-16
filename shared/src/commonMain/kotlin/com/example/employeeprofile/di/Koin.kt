package com.example.employeeprofile.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Bindings both platforms share — repositories, view models, anything in commonMain. */
val sharedModule = module {
}

/**
 * What each platform brings of its own: the database driver, file storage and the media
 * pickers. Declared here so [initKoin] can load it without knowing which platform it's on.
 */
expect val platformModule: Module

/**
 * Starts Koin once per process. Android hands its context in through [appDeclaration];
 * iOS goes through `startKoinForIos` instead, since default arguments don't survive the
 * Objective-C export.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(sharedModule, platformModule)
}
