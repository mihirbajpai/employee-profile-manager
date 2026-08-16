package com.example.employeeprofile.view.screen.summary

import androidx.lifecycle.ViewModel
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.DepartmentSummary
import com.example.employeeprofile.domain.algo.summariseByDepartment
import com.example.employeeprofile.view.asScreenState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/** Totals per department, recomputed whenever the roster changes. */
class DepartmentSummaryViewModel(repository: EmployeeRepository) : ViewModel() {

    val summaries: StateFlow<List<DepartmentSummary>> = repository.observeAll()
        .map(::summariseByDepartment)
        .asScreenState(this, emptyList())
}
