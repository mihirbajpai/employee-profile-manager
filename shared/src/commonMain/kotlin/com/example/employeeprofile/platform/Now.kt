package com.example.employeeprofile.platform

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Epoch millis, for the createdAt / updatedAt stamps and the "not a future date" check. */
@OptIn(ExperimentalTime::class)
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
