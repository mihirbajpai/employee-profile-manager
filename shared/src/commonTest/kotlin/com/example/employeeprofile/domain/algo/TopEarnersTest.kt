package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopEarnersTest {

    private val staff = listOf(
        employee(id = 1, fullName = "A", salary = 400_000.0),
        employee(id = 2, fullName = "B", salary = 2_400_000.0),
        employee(id = 3, fullName = "C", salary = 1_250_000.0),
        employee(id = 4, fullName = "D", salary = 690_000.0),
        employee(id = 5, fullName = "E", salary = 840_000.0)
    )

    @Test
    fun `returns the highest paid first`() {
        val top = topNBySalary(staff, n = 3)
        assertEquals(listOf("B", "C", "E"), top.map { it.fullName })
    }

    @Test
    fun `returns exactly n entries when there are more than n employees`() {
        assertEquals(2, topNBySalary(staff, n = 2).size)
    }

    @Test
    fun `returns everyone when n is larger than the list`() {
        val top = topNBySalary(staff, n = 50)
        assertEquals(staff.size, top.size)
        assertEquals("B", top.first().fullName)
    }

    @Test
    fun `an empty roster produces nothing`() {
        assertTrue(topNBySalary(emptyList(), n = 5).isEmpty())
    }

    @Test
    fun `n of zero or less produces nothing`() {
        assertTrue(topNBySalary(staff, n = 0).isEmpty())
        assertTrue(topNBySalary(staff, n = -1).isEmpty())
    }

    @Test
    fun `the default n is five`() {
        assertEquals(DEFAULT_TOP_COUNT, topNBySalary(staff).size)
    }

    @Test
    fun `agrees with a full sort which is the slow way of asking the same question`() {
        val byHeap = topNBySalary(staff, n = 3).map { it.fullName }
        val bySort = staff.sortedByDescending { it.salary }.take(3).map { it.fullName }
        assertEquals(bySort, byHeap)
    }

    @Test
    fun `ties do not lose anyone`() {
        val tied = listOf(
            employee(id = 1, fullName = "A", salary = 500_000.0),
            employee(id = 2, fullName = "B", salary = 500_000.0),
            employee(id = 3, fullName = "C", salary = 100_000.0)
        )
        val top = topNBySalary(tied, n = 2)
        assertEquals(2, top.size)
        assertEquals(setOf("A", "B"), top.map { it.fullName }.toSet())
    }
}
