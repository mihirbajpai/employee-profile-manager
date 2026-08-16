package com.example.employeeprofile.view.screen.form

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the form hands to storage, and what it gets back when editing. The tidying done here —
 * trimming, lower-casing, deriving the normalised phone — is invisible in the UI, so it only
 * shows up as a bug much later.
 */
class EmployeeFormStateTest {

    private val filled = EmployeeFormState(
        fullName = "  Priya Sharma  ",
        email = "  PRIYA@ACME.IO  ",
        phone = "+91 98765 43210",
        address = "  12 MG Road  ",
        gender = Gender.FEMALE,
        department = Department.ENGINEERING,
        skills = setOf(Skill.KOTLIN, Skill.ANDROID),
        employmentType = EmploymentType.FULL_TIME,
        joiningDate = 1_700_000_000_000L,
        salary = "1250000"
    )

    @Test
    fun `text is trimmed on the way to storage`() {
        val saved = filled.toEmployee()
        assertEquals("Priya Sharma", saved.fullName)
        assertEquals("12 MG Road", saved.address)
    }

    @Test
    fun `email is lower-cased before storing`() {
        assertEquals("priya@acme.io", filled.toEmployee().email)
    }

    @Test
    fun `the normalised phone is derived while the typed one is kept`() {
        val saved = filled.toEmployee()
        assertEquals("+91 98765 43210", saved.phone)
        assertEquals("9876543210", saved.normalizedPhone)
    }

    @Test
    fun `salary is parsed to a number`() {
        assertEquals(1_250_000.0, filled.toEmployee().salary)
    }

    @Test
    fun `creating leaves the id and createdAt for the repository to set`() {
        val saved = filled.toEmployee()
        assertEquals(0L, saved.id)
        assertEquals(0L, saved.createdAt)
    }

    @Test
    fun `editing keeps the original id and createdAt`() {
        val existing = employee(id = 42, createdAt = 999L)
        val saved = filled.toEmployee(existing)
        assertEquals(42L, saved.id)
        assertEquals(999L, saved.createdAt)
    }

    @Test
    fun `a stored record fills the form`() {
        val form = employee(
            fullName = "Karan Nair",
            email = "karan@acme.io",
            skills = listOf(Skill.KOTLIN, Skill.BACKEND)
        ).toFormState()
        assertEquals("Karan Nair", form.fullName)
        assertEquals("karan@acme.io", form.email)
        assertEquals(setOf(Skill.KOTLIN, Skill.BACKEND), form.skills)
    }

    /** A whole salary shouldn't reappear in the field as "1250000.0". */
    @Test
    fun `a whole salary comes back without a decimal point`() {
        assertEquals("1250000", employee(salary = 1_250_000.0).toFormState().salary)
    }

    @Test
    fun `a salary with paise keeps them`() {
        assertEquals("1250000.5", employee(salary = 1_250_000.5).toFormState().salary)
    }

}
