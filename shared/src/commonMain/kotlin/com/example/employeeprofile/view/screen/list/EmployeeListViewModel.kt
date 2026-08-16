package com.example.employeeprofile.view.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.repository.EmployeeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

/**
 * Holds what the list screen renders. Search, filtering and sorting land here too — the
 * composable only draws what it receives.
 */
@OptIn(FlowPreview::class)
class EmployeeListViewModel(repository: EmployeeRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val employees: StateFlow<List<Employee>> = combine(
        repository.observeAll(),
        // Debouncing an empty query would delay the first frame for no reason.
        _searchQuery.debounce { if (it.isEmpty()) 0.milliseconds else SEARCH_DEBOUNCE }
    ) { all, query -> all.filter { it.matches(query) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Name, email and department are searched together, as one case-insensitive contains. */
    private fun Employee.matches(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        return fullName.contains(trimmed, ignoreCase = true) ||
            email.contains(trimmed, ignoreCase = true) ||
            department.label.contains(trimmed, ignoreCase = true)
    }

    private companion object {
        /** Long enough to skip the letters someone types on the way to a word. */
        val SEARCH_DEBOUNCE = 300.milliseconds

        /** Keeps the database subscription alive across a configuration change. */
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
