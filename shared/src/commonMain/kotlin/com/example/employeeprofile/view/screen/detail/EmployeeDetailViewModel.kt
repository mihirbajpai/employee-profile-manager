package com.example.employeeprofile.view.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.view.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Watches one employee rather than reading it once, so an edit made on the form is reflected
 * the moment the user comes back — and a delete turns into the not-found state instead of
 * leaving a stale record on screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EmployeeDetailViewModel(repository: EmployeeRepository) : ViewModel() {

    private val employeeId = MutableStateFlow(Employee.NO_ID)

    val employee: StateFlow<DataState<Employee>> = employeeId
        .filter { it != Employee.NO_ID }
        .flatMapLatest { id -> repository.observeById(id) }
        .map { found ->
            if (found == null) {
                DataState.Failure(IllegalStateException("That employee no longer exists"))
            } else {
                DataState.Success(found)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = DataState.Loading()
        )

    fun load(id: Long) {
        employeeId.value = id
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
