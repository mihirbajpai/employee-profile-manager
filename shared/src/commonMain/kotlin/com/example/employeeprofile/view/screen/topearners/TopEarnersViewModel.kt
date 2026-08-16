package com.example.employeeprofile.view.screen.topearners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.DEFAULT_TOP_COUNT
import com.example.employeeprofile.domain.algo.MAX_TOP_COUNT
import com.example.employeeprofile.domain.algo.MIN_TOP_COUNT
import com.example.employeeprofile.domain.algo.topNBySalary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    /** Steps N by [delta], clamped to the range the stepper offers. */
    fun onTopCountChange(delta: Int) {
        _topCount.value = (_topCount.value + delta).coerceIn(MIN_TOP_COUNT, MAX_TOP_COUNT)
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
