package com.example.employeeprofile.view.screen.topearners

import androidx.lifecycle.ViewModel
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.DEFAULT_TOP_COUNT
import com.example.employeeprofile.domain.algo.MAX_TOP_COUNT
import com.example.employeeprofile.domain.algo.MIN_TOP_COUNT
import com.example.employeeprofile.domain.algo.topNBySalary
import com.example.employeeprofile.view.asScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

/**
 * The Top Earners section. N is combined with the employee flow, so changing the stepper or
 * editing somebody's salary both recompute without the screen asking for anything.
 */
class TopEarnersViewModel(repository: EmployeeRepository) : ViewModel() {

    private val _topCount = MutableStateFlow(DEFAULT_TOP_COUNT)
    val topCount: StateFlow<Int> = _topCount.asStateFlow()

    val topEarners: StateFlow<List<Employee>> =
        combine(repository.observeAll(), _topCount) { employees, count ->
            topNBySalary(employees, count)
        }.asScreenState(this, emptyList())

    /** Steps N by [delta], clamped to the range the stepper offers. */
    fun onTopCountChange(delta: Int) {
        _topCount.value = (_topCount.value + delta).coerceIn(MIN_TOP_COUNT, MAX_TOP_COUNT)
    }
}
