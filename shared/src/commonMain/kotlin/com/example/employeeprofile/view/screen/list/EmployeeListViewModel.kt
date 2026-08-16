package com.example.employeeprofile.view.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Holds what the list screen renders. Search, filtering and sorting land here too — the
 * composable only draws what it receives.
 */
class EmployeeListViewModel(repository: EmployeeRepository) : ViewModel() {

    val employees: StateFlow<List<Employee>> = repository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    private companion object {
        /** Keeps the database subscription alive across a configuration change. */
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
