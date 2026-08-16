package com.example.employeeprofile.view.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.DepartmentSummary
import com.example.employeeprofile.domain.algo.summariseByDepartment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Totals per department, recomputed whenever the roster changes. */
class DepartmentSummaryViewModel(repository: EmployeeRepository) : ViewModel() {

    val summaries: StateFlow<List<DepartmentSummary>> = repository.observeAll()
        .map(::summariseByDepartment)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
