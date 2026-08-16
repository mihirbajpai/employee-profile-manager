package com.example.employeeprofile.view.screen.list

import com.example.employeeprofile.data.model.Employee

/**
 * How the list is ordered. Each entry carries its own comparator so the view model sorts by
 * `sort.comparator` without a `when` over every case.
 *
 * Names compare case-insensitively — otherwise "arjun" would sort after "Zara".
 */
enum class EmployeeSort(val label: String, val comparator: Comparator<Employee>) {
    NAME_ASC("Name (A–Z)", compareBy { it.fullName.lowercase() }),
    NAME_DESC("Name (Z–A)", compareByDescending { it.fullName.lowercase() }),
    JOINED_NEWEST("Joining date (newest)", compareByDescending { it.joiningDate }),
    JOINED_OLDEST("Joining date (oldest)", compareBy { it.joiningDate }),
    SALARY_HIGH("Salary (high to low)", compareByDescending { it.salary }),
    SALARY_LOW("Salary (low to high)", compareBy { it.salary })
}
