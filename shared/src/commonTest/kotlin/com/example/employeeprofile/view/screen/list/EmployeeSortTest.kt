package com.example.employeeprofile.view.screen.list

import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals

class EmployeeSortTest {

    private val roster = listOf(
        employee(fullName = "arjun mehta", salary = 800_000.0, joiningDate = 3_000L),
        employee(fullName = "Zara Khan", salary = 200_000.0, joiningDate = 1_000L),
        employee(fullName = "Priya Sharma", salary = 500_000.0, joiningDate = 2_000L)
    )

    private fun sortedBy(sort: EmployeeSort) =
        roster.sortedWith(sort.comparator).map { it.fullName }

    /** Otherwise a lower-case name sorts after every capitalised one. */
    @Test
    fun `name ordering ignores case`() {
        assertEquals(
            listOf("arjun mehta", "Priya Sharma", "Zara Khan"),
            sortedBy(EmployeeSort.NAME_ASC)
        )
    }

    @Test
    fun `name can be reversed`() {
        assertEquals(
            listOf("Zara Khan", "Priya Sharma", "arjun mehta"),
            sortedBy(EmployeeSort.NAME_DESC)
        )
    }

    @Test
    fun `newest joiner first`() {
        assertEquals(
            listOf("arjun mehta", "Priya Sharma", "Zara Khan"),
            sortedBy(EmployeeSort.JOINED_NEWEST)
        )
    }

    @Test
    fun `oldest joiner first`() {
        assertEquals(
            listOf("Zara Khan", "Priya Sharma", "arjun mehta"),
            sortedBy(EmployeeSort.JOINED_OLDEST)
        )
    }

    @Test
    fun `highest paid first`() {
        assertEquals(
            listOf("arjun mehta", "Priya Sharma", "Zara Khan"),
            sortedBy(EmployeeSort.SALARY_HIGH)
        )
    }

    @Test
    fun `lowest paid first`() {
        assertEquals(
            listOf("Zara Khan", "Priya Sharma", "arjun mehta"),
            sortedBy(EmployeeSort.SALARY_LOW)
        )
    }

    @Test
    fun `every option the menu offers carries a comparator and a label`() {
        assertEquals(6, EmployeeSort.entries.size)
        EmployeeSort.entries.forEach { sort ->
            assertEquals(roster.size, roster.sortedWith(sort.comparator).size)
            assertEquals(true, sort.label.isNotBlank())
        }
    }
}
