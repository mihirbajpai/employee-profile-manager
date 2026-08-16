package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee

/** Headcount and pay for one department. */
data class DepartmentSummary(
    val department: Department,
    val headcount: Int,
    val activeCount: Int,
    val totalSalary: Double,
    val averageSalary: Double
)

/**
 * Groups the roster by department and totals it up.
 *
 * Time complexity: O(n) where n = number of employees — a single pass, with one O(1) hash
 * lookup per employee to find its department's running totals. Sorting the result afterwards is
 * O(d log d) over the handful of departments, which doesn't move the bound.
 * Space complexity: O(d) where d = number of departments, not O(n) — the accumulator holds one
 * entry per department however many employees there are.
 *
 * Departments with nobody in them are left out rather than reported as zero rows, so the screen
 * shows the shape of the company rather than the shape of the enum.
 */
fun summariseByDepartment(employees: List<Employee>): List<DepartmentSummary> {
    if (employees.isEmpty()) return emptyList()

    val headcount = mutableMapOf<Department, Int>()
    val active = mutableMapOf<Department, Int>()
    val payroll = mutableMapOf<Department, Double>()

    for (employee in employees) {
        val department = employee.department
        headcount[department] = (headcount[department] ?: 0) + 1
        payroll[department] = (payroll[department] ?: 0.0) + employee.salary
        if (employee.isActive) active[department] = (active[department] ?: 0) + 1
    }

    return headcount.map { (department, count) ->
        val total = payroll[department] ?: 0.0
        DepartmentSummary(
            department = department,
            headcount = count,
            activeCount = active[department] ?: 0,
            totalSalary = total,
            averageSalary = total / count
        )
    }.sortedByDescending { it.headcount }
}
