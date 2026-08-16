package com.example.employeeprofile.view.screen.list

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType

/**
 * What the list is narrowed to. An empty set means that dimension isn't restricted, so the
 * filters compose with AND — an employee has to satisfy every restriction that is set.
 */
data class EmployeeFilters(
    val departments: Set<Department> = emptySet(),
    val employmentTypes: Set<EmploymentType> = emptySet(),
    val isActive: Boolean? = null
) {
    /** Number on the filter badge: every individual selection, not every restricted dimension. */
    val selectionCount: Int =
        departments.size + employmentTypes.size + if (isActive == null) 0 else 1

    fun matches(employee: Employee): Boolean =
        (departments.isEmpty() || employee.department in departments) &&
            (employmentTypes.isEmpty() || employee.employmentType in employmentTypes) &&
            (isActive == null || employee.isActive == isActive)
}
