package com.example.employeeprofile.view.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.UndoStack
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Holds what the list screen renders. Search, filtering and sorting land here too — the
 * composable only draws what it receives.
 */
@OptIn(FlowPreview::class)
class EmployeeListViewModel(private val repository: EmployeeRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(EmployeeFilters())
    val filters: StateFlow<EmployeeFilters> = _filters.asStateFlow()

    /** Deleted records, newest first, so a mistaken delete can be taken back. */
    private val deleted = UndoStack<Employee>()

    /** The employee the undo snackbar is currently offering to bring back, if any. */
    private val _undoPrompt = MutableStateFlow<Employee?>(null)
    val undoPrompt: StateFlow<Employee?> = _undoPrompt.asStateFlow()

    /** Held here rather than in the composable, so it survives navigating away and back. */
    private val _sort = MutableStateFlow(EmployeeSort.NAME_ASC)
    val sort: StateFlow<EmployeeSort> = _sort.asStateFlow()

    /**
     * Search, filters and sort are folded together here, so the screen never re-derives
     * anything and any one of them changing re-emits the finished list.
     */
    val employees: StateFlow<List<Employee>> = combine(
        repository.observeAll(),
        // Debouncing an empty query would delay the first frame for no reason.
        _searchQuery.debounce { if (it.isEmpty()) 0.milliseconds else SEARCH_DEBOUNCE },
        _filters,
        _sort
    ) { all, query, filters, sort ->
        all.filter { it.matches(query) && filters.matches(it) }.sortedWith(sort.comparator)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onToggleDepartment(department: Department) {
        _filters.value = _filters.value.let {
            it.copy(departments = it.departments.toggle(department))
        }
    }

    fun onToggleEmploymentType(type: EmploymentType) {
        _filters.value = _filters.value.let {
            it.copy(employmentTypes = it.employmentTypes.toggle(type))
        }
    }

    /** null clears the status restriction; tapping the selected option clears it too. */
    fun onStatusChange(isActive: Boolean?) {
        _filters.value = _filters.value.copy(
            isActive = if (_filters.value.isActive == isActive) null else isActive
        )
    }

    fun onDelete(employee: Employee) {
        viewModelScope.launch {
            repository.delete(employee)
            deleted.push(employee)
            _undoPrompt.value = employee
        }
    }

    /** Pops the most recent deletion and puts it back. */
    fun onUndoDelete() {
        viewModelScope.launch {
            deleted.pop()?.let { repository.restore(it) }
            _undoPrompt.value = null
        }
    }

    /** The snackbar timed out or was dismissed; the record stays deleted. */
    fun onUndoPromptShown() {
        _undoPrompt.value = null
    }

    fun onSortChange(sort: EmployeeSort) {
        _sort.value = sort
    }

    fun onClearFilters() {
        _filters.value = EmployeeFilters()
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (value in this) this - value else this + value

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
