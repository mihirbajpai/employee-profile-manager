package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DepartmentSummaryTest {

    private val roster = listOf(
        employee(department = Department.ENGINEERING, salary = 1_000_000.0, isActive = true),
        employee(department = Department.ENGINEERING, salary = 2_000_000.0, isActive = true),
        employee(department = Department.ENGINEERING, salary = 3_000_000.0, isActive = false),
        employee(department = Department.DESIGN, salary = 500_000.0, isActive = true)
    )

    private fun summaryFor(department: Department) =
        summariseByDepartment(roster).firstOrNull { it.department == department }

    @Test
    fun `headcount counts everyone in the department`() {
        assertEquals(3, summaryFor(Department.ENGINEERING)?.headcount)
        assertEquals(1, summaryFor(Department.DESIGN)?.headcount)
    }

    @Test
    fun `active count excludes the inactive`() {
        assertEquals(2, summaryFor(Department.ENGINEERING)?.activeCount)
    }

    @Test
    fun `totals and averages are over everyone not just the active`() {
        val engineering = summaryFor(Department.ENGINEERING)
        assertEquals(6_000_000.0, engineering?.totalSalary)
        assertEquals(2_000_000.0, engineering?.averageSalary)
    }

    /** A department nobody works in is left out rather than reported as a row of zeroes. */
    @Test
    fun `empty departments are not reported`() {
        assertNull(summaryFor(Department.HR))
        assertEquals(2, summariseByDepartment(roster).size)
    }

    @Test
    fun `the biggest department comes first`() {
        assertEquals(Department.ENGINEERING, summariseByDepartment(roster).first().department)
    }

    @Test
    fun `an empty roster summarises to nothing`() {
        assertTrue(summariseByDepartment(emptyList()).isEmpty())
    }

    @Test
    fun `every employee is accounted for exactly once`() {
        assertEquals(roster.size, summariseByDepartment(roster).sumOf { it.headcount })
    }
}
