package com.example.employeeprofile.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The skills column is the one place the app flattens a list into a string. If this stops
 * round-tripping, skills quietly disappear from saved records rather than failing loudly.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `a list survives the trip out and back`() {
        val skills = listOf("KOTLIN", "ANDROID", "BACKEND")
        assertEquals(skills, converters.toSkills(converters.fromSkills(skills)))
    }

    @Test
    fun `no skills is stored as an empty column rather than a stray separator`() {
        assertEquals("", converters.fromSkills(emptyList()))
    }

    @Test
    fun `an empty or blank column reads back as no skills`() {
        assertTrue(converters.toSkills("").isEmpty())
        assertTrue(converters.toSkills("   ").isEmpty())
    }
}
