package com.example.employeeprofile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * How long a flow keeps running after the screen stops collecting it. Long enough to survive a
 * configuration change without re-querying the database, short enough that a screen left behind
 * doesn't keep working.
 */
private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L

/**
 * Shares a flow as screen state on the view model's own scope.
 *
 * Every view model wants the same three arguments, and repeating them invites one of them to
 * quietly differ from the rest.
 */
fun <T> Flow<T>.asScreenState(viewModel: ViewModel, initialValue: T): StateFlow<T> = stateIn(
    scope = viewModel.viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
    initialValue = initialValue
)
