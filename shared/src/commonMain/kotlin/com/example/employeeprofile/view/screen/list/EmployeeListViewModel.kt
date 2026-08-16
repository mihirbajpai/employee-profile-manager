package com.example.employeeprofile.view.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.NameTrie
import com.example.employeeprofile.domain.algo.RecentlyViewed
import com.example.employeeprofile.domain.algo.UndoStack
import com.example.employeeprofile.view.asScreenState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Holds what the list screen renders. Search, filtering and sorting land here too — the
 * composable only draws what it receives.
 */
@OptIn(FlowPreview::class)
class EmployeeListViewModel(
    private val repository: EmployeeRepository,
    private val recentlyViewed: RecentlyViewed
) : ViewModel() {

    /** Rebuilt whenever the roster changes; drives the type-ahead suggestions. */
    private val nameTrie = NameTrie()

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

    /** How many rows are on screen; grows a page at a time as the list is scrolled. */
    private val _visibleCount = MutableStateFlow(PAGE_SIZE)

    /**
     * Search, filters and sort are folded together here, so the screen never re-derives
     * anything and any one of them changing re-emits the finished list.
     */
    private val matching: Flow<List<Employee>> = combine(
        repository.observeAll(),
        // Debouncing an empty query would delay the first frame for no reason.
        _searchQuery.debounce { if (it.isEmpty()) 0.milliseconds else SEARCH_DEBOUNCE },
        _filters,
        _sort
    ) { all, query, filters, sort ->
        all.filter { it.matches(query) && filters.matches(it) }.sortedWith(sort.comparator)
    }

    /**
     * Everything the current search and filters match, in sort order and with no page limit.
     * This is what an export writes out — the page boundary is a drawing concern, and silently
     * exporting only the rows that happen to be on screen would be a lie.
     */
    val matchingAll: StateFlow<List<Employee>> = matching.asScreenState(this, emptyList())

    /**
     * A page at a time, taken after filtering and sorting rather than before.
     *
     * The brief suggests paging in the DAO. That would hand the view model one page at a time,
     * and searching or sorting would then only see that page — which contradicts the list
     * screen's requirement that those run across every record. So the query stays whole and
     * the page boundary is applied last, where it only affects how much is drawn.
     */
    val employees: StateFlow<List<Employee>> = combine(matchingAll, _visibleCount) { list, count ->
        list.take(count)
    }.asScreenState(this, emptyList())

    /** Whether anything is left below what's drawn — drives the spinner at the list's foot. */
    val hasMore: StateFlow<Boolean> = combine(matchingAll, _visibleCount) { list, count ->
        list.size > count
    }.asScreenState(this, false)

    /**
     * Names starting with what's been typed, from the trie. Empty once the query matches
     * nothing new to offer — there's no point suggesting a name that's already been typed out.
     */
    val suggestions: StateFlow<List<String>> = combine(
        matchingAll,
        repository.observeAll(),
        _searchQuery
    ) { _, all, query ->
        nameTrie.reset(all)
        if (query.trim().length < MIN_PREFIX) {
            emptyList()
        } else {
            nameTrie.suggest(query).filterNot { it.equals(query.trim(), ignoreCase = true) }
        }
    }.asScreenState(this, emptyList())

    /** The employees opened most recently, newest first, minus anyone since deleted. */
    val recent: StateFlow<List<Employee>> = repository.observeAll().map { all ->
        recentlyViewed.retainAll(all.map { it.id }.toSet())
        val byId = all.associateBy { it.id }
        recentlyViewed.ids().mapNotNull(byId::get)
    }.asScreenState(this, emptyList())

    /** Called when the list nears its end. */
    fun onLoadMore() {
        _visibleCount.value += PAGE_SIZE
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        resetPaging()
    }

    fun onToggleDepartment(department: Department) {
        _filters.value = _filters.value.let {
            it.copy(departments = it.departments.toggle(department))
        }
        resetPaging()
    }

    fun onToggleEmploymentType(type: EmploymentType) {
        _filters.value = _filters.value.let {
            it.copy(employmentTypes = it.employmentTypes.toggle(type))
        }
        resetPaging()
    }

    /** null clears the status restriction; tapping the selected option clears it too. */
    fun onStatusChange(isActive: Boolean?) {
        _filters.value = _filters.value.copy(
            isActive = if (_filters.value.isActive == isActive) null else isActive
        )
        resetPaging()
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

    /** Fills the search box from a suggestion. */
    fun onSuggestionChosen(name: String) {
        onSearchQueryChange(name)
    }

    fun onSortChange(sort: EmployeeSort) {
        _sort.value = sort
        resetPaging()
    }

    fun onClearFilters() {
        _filters.value = EmployeeFilters()
        resetPaging()
    }

    /** A changed query, filter or order means the old page boundary means nothing. */
    private fun resetPaging() {
        _visibleCount.value = PAGE_SIZE
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
        /** Rows per page, per the brief. */
        const val PAGE_SIZE = 20

        /** Below this, a prefix matches so much that suggesting anything is noise. */
        const val MIN_PREFIX = 2

        /** Long enough to skip the letters someone types on the way to a word. */
        val SEARCH_DEBOUNCE = 300.milliseconds
    }
}
