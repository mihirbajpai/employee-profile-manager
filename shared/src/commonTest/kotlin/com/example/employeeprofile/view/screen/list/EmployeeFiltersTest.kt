package com.example.employeeprofile.view.screen.list

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmployeeFiltersTest {

    private val engineer = employee(
        department = Department.ENGINEERING,
        employmentType = EmploymentType.FULL_TIME,
        isActive = true
    )
    private val designer = employee(
        department = Department.DESIGN,
        employmentType = EmploymentType.CONTRACT,
        isActive = false
    )

    @Test
    fun `no filters admits everyone`() {
        val filters = EmployeeFilters()
        assertTrue(filters.matches(engineer))
        assertTrue(filters.matches(designer))
    }

    @Test
    fun `a department filter admits only that department`() {
        val filters = EmployeeFilters(departments = setOf(Department.DESIGN))
        assertFalse(filters.matches(engineer))
        assertTrue(filters.matches(designer))
    }

    @Test
    fun `several departments are an or within that dimension`() {
        val filters = EmployeeFilters(
            departments = setOf(Department.DESIGN, Department.ENGINEERING)
        )
        assertTrue(filters.matches(engineer))
        assertTrue(filters.matches(designer))
    }

    @Test
    fun `status narrows to active or inactive`() {
        assertTrue(EmployeeFilters(isActive = true).matches(engineer))
        assertFalse(EmployeeFilters(isActive = true).matches(designer))
        assertTrue(EmployeeFilters(isActive = false).matches(designer))
    }

    /** The brief's AND: an employee has to satisfy every dimension that is set. */
    @Test
    fun `dimensions combine with and`() {
        val matching = EmployeeFilters(
            departments = setOf(Department.DESIGN),
            employmentTypes = setOf(EmploymentType.CONTRACT),
            isActive = false
        )
        assertTrue(matching.matches(designer))

        // Same as above but demanding active, which the designer is not.
        assertFalse(matching.copy(isActive = true).matches(designer))
        // Right status and type, wrong department.
        assertFalse(matching.copy(departments = setOf(Department.HR)).matches(designer))
    }

    @Test
    fun `the badge counts every selection rather than every dimension`() {
        assertEquals(0, EmployeeFilters().selectionCount)
        assertEquals(
            4,
            EmployeeFilters(
                departments = setOf(Department.DESIGN, Department.HR),
                employmentTypes = setOf(EmploymentType.CONTRACT),
                isActive = true
            ).selectionCount
        )
    }
}
