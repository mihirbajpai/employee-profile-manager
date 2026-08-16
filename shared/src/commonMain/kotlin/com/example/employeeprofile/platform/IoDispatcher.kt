package com.example.employeeprofile.platform

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Where file and database work belongs. Kotlin/Native has no separate IO pool, so there it is
 * the default dispatcher — which is already a thread pool, not the main thread.
 */
expect val ioDispatcher: CoroutineDispatcher
