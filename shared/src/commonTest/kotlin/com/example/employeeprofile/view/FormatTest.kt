package com.example.employeeprofile.view

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    @Test
    fun `salary groups the last three digits then in pairs`() {
        assertEquals("₹12,50,000", formatSalary(1_250_000.0))
        assertEquals("₹24,00,000", formatSalary(2_400_000.0))
    }

    @Test
    fun `salaries below a thousand are not grouped`() {
        assertEquals("₹999", formatSalary(999.0))
    }

    @Test
    fun `paise are dropped`() {
        assertEquals("₹1,000", formatSalary(1000.49))
    }

    @Test
    fun `dates read as day short month and year`() {
        // 2024-03-05T00:00:00Z
        assertEquals("05 Mar 2024", formatDate(1_709_596_800_000L))
    }

    @Test
    fun `initials take the first letter of the first two words`() {
        assertEquals("PS", initialsOf("Priya Sharma"))
        assertEquals("AM", initialsOf("arjun mehta"))
    }

    @Test
    fun `initials cope with one name extra spaces and nothing at all`() {
        assertEquals("P", initialsOf("Priya"))
        assertEquals("PS", initialsOf("  Priya   Sharma  "))
        assertEquals("?", initialsOf("   "))
    }

    @Test
    fun `ordinals use the right suffix`() {
        assertEquals("1st", ordinal(1))
        assertEquals("2nd", ordinal(2))
        assertEquals("3rd", ordinal(3))
        assertEquals("4th", ordinal(4))
    }

    @Test
    fun `the teens are all th`() {
        assertEquals("11th", ordinal(11))
        assertEquals("12th", ordinal(12))
        assertEquals("13th", ordinal(13))
        assertEquals("21st", ordinal(21))
    }

    @Test
    fun `file sizes switch from KB to MB`() {
        assertEquals("1.0 KB", formatFileSize(1024))
        assertEquals("1.5 KB", formatFileSize(1536))
        assertEquals("1.0 MB", formatFileSize(1024L * 1024))
        assertEquals("5.0 MB", formatFileSize(5L * 1024 * 1024))
    }
}
