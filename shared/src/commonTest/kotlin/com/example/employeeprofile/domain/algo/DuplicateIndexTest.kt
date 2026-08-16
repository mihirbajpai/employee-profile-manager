package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DuplicateIndexTest {

    private fun indexOf(vararg employees: com.example.employeeprofile.data.model.Employee) =
        DuplicateIndex().apply { reset(employees.toList()) }

    @Test
    fun `a record that clashes with nothing is free to save`() {
        val index = indexOf(employee(id = 1))
        assertNull(index.findConflict("new@acme.io", "9000000000", "New Person"))
    }

    @Test
    fun `an email already taken is reported`() {
        val index = indexOf(employee(id = 1, email = "priya@acme.io"))
        assertEquals(
            DuplicateField.EMAIL,
            index.findConflict("priya@acme.io", "9000000000", "Someone Else")
        )
    }

    @Test
    fun `email comparison ignores case`() {
        val index = indexOf(employee(id = 1, email = "priya@acme.io"))
        assertEquals(
            DuplicateField.EMAIL,
            index.findConflict("PRIYA@ACME.IO", "9000000000", "Someone Else")
        )
    }

    @Test
    fun `a phone already taken is reported`() {
        val index = indexOf(employee(id = 1, phone = "9876543210"))
        assertEquals(
            DuplicateField.PHONE,
            index.findConflict("new@acme.io", "9876543210", "Someone Else")
        )
    }

    @Test
    fun `the same number typed differently still counts as taken`() {
        val index = indexOf(employee(id = 1, phone = "+91 98765 43210"))
        assertEquals(
            DuplicateField.PHONE,
            index.findConflict("new@acme.io", normalizePhone("098765-43210"), "Someone Else")
        )
    }

    @Test
    fun `a name already taken is reported`() {
        val index = indexOf(employee(id = 1, fullName = "Priya Sharma"))
        assertEquals(
            DuplicateField.NAME,
            index.findConflict("new@acme.io", "9000000000", "  priya sharma  ")
        )
    }

    /** Editing a record must not flag it as a duplicate of itself. */
    @Test
    fun `a record does not conflict with its own stored values`() {
        val existing = employee(id = 7)
        val index = indexOf(existing)
        assertNull(
            index.findConflict(
                email = existing.email,
                normalizedPhone = existing.normalizedPhone,
                fullName = existing.fullName,
                selfId = existing.id
            )
        )
    }

    @Test
    fun `removing a record frees its email again`() {
        val existing = employee(id = 1)
        val index = indexOf(existing)
        index.remove(existing)
        assertNull(index.findConflict(existing.email, "9000000000", "Someone Else"))
    }

    @Test
    fun `adding a record takes its email`() {
        val index = DuplicateIndex()
        index.add(employee(id = 1, email = "priya@acme.io"))
        assertEquals(
            DuplicateField.EMAIL,
            index.findConflict("priya@acme.io", "9000000000", "Someone Else")
        )
    }

    @Test
    fun `isDuplicate answers yes for a taken email or phone`() {
        val index = indexOf(employee(id = 1, email = "priya@acme.io", phone = "9876543210"))
        assertTrue(index.isDuplicate("priya@acme.io", "9000000000"))
        assertTrue(index.isDuplicate("free@acme.io", "9876543210"))
        assertFalse(index.isDuplicate("free@acme.io", "9000000000"))
    }

    @Test
    fun `isDuplicate does not count the record being edited against itself`() {
        val existing = employee(id = 7)
        val index = indexOf(existing)
        assertFalse(index.isDuplicate(existing.email, existing.normalizedPhone, selfId = 7))
        assertTrue(index.isDuplicate(existing.email, existing.normalizedPhone, selfId = 8))
    }

    @Test
    fun `reset replaces everything the index knew`() {
        val index = indexOf(employee(id = 1, email = "old@acme.io"))
        index.reset(listOf(employee(id = 2, email = "new@acme.io")))
        assertNull(index.findConflict("old@acme.io", "9000000000", "Nobody At All"))
        assertEquals(
            DuplicateField.EMAIL,
            index.findConflict("new@acme.io", "9000000000", "Nobody At All")
        )
    }
}
