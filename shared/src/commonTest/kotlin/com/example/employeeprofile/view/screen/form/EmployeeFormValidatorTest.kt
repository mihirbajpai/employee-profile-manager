package com.example.employeeprofile.view.screen.form

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.platform.nowMillis
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmployeeFormValidatorTest {

    /** A form with nothing wrong with it; each test spoils exactly one field. */
    private val valid = EmployeeFormState(
        fullName = "Priya Sharma",
        email = "priya@acme.io",
        phone = "9876543210",
        address = "12 MG Road, Bengaluru",
        gender = Gender.FEMALE,
        department = Department.ENGINEERING,
        skills = setOf(Skill.KOTLIN),
        employmentType = EmploymentType.FULL_TIME,
        joiningDate = 1_700_000_000_000L,
        salary = "1000000"
    )

    private fun errorsFor(state: EmployeeFormState) = EmployeeFormValidator.validate(state)

    @Test
    fun `a complete form has nothing wrong with it`() {
        assertTrue(errorsFor(valid).isEmpty())
    }

    @Test
    fun `full name is required`() {
        assertContains(errorsFor(valid.copy(fullName = "   ")), FormField.FULL_NAME)
    }

    @Test
    fun `full name must be at least three characters`() {
        assertContains(errorsFor(valid.copy(fullName = "Jo")), FormField.FULL_NAME)
    }

    @Test
    fun `full name is measured after trimming`() {
        assertContains(errorsFor(valid.copy(fullName = "  Jo  ")), FormField.FULL_NAME)
        assertFalse(FormField.FULL_NAME in errorsFor(valid.copy(fullName = "  Joe  ")))
    }

    @Test
    fun `email must look like an email`() {
        assertContains(errorsFor(valid.copy(email = "priya")), FormField.EMAIL)
        assertContains(errorsFor(valid.copy(email = "priya@acme")), FormField.EMAIL)
        assertContains(errorsFor(valid.copy(email = "@acme.io")), FormField.EMAIL)
    }

    @Test
    fun `email is required`() {
        assertContains(errorsFor(valid.copy(email = "")), FormField.EMAIL)
    }

    @Test
    fun `phone must be ten digits`() {
        assertContains(errorsFor(valid.copy(phone = "98765")), FormField.PHONE)
        assertContains(errorsFor(valid.copy(phone = "98765432100000")), FormField.PHONE)
    }

    @Test
    fun `phone is judged after normalising so punctuation is fine`() {
        assertTrue(errorsFor(valid.copy(phone = "+91 98765 43210")).isEmpty())
    }

    @Test
    fun `address must be at least six characters`() {
        assertContains(errorsFor(valid.copy(address = "12 MG")), FormField.ADDRESS)
    }

    @Test
    fun `every required choice must be made`() {
        assertContains(errorsFor(valid.copy(gender = null)), FormField.GENDER)
        assertContains(errorsFor(valid.copy(department = null)), FormField.DEPARTMENT)
        assertContains(errorsFor(valid.copy(employmentType = null)), FormField.EMPLOYMENT_TYPE)
    }

    @Test
    fun `at least one skill must be selected`() {
        assertContains(errorsFor(valid.copy(skills = emptySet())), FormField.SKILLS)
    }

    @Test
    fun `joining date is required`() {
        assertContains(errorsFor(valid.copy(joiningDate = null)), FormField.JOINING_DATE)
    }

    @Test
    fun `joining date cannot be in the future`() {
        val tomorrow = nowMillis() + 24 * 60 * 60 * 1000
        assertContains(errorsFor(valid.copy(joiningDate = tomorrow)), FormField.JOINING_DATE)
    }

    @Test
    fun `salary must be a positive number`() {
        assertContains(errorsFor(valid.copy(salary = "")), FormField.SALARY)
        assertContains(errorsFor(valid.copy(salary = "abc")), FormField.SALARY)
        assertContains(errorsFor(valid.copy(salary = "0")), FormField.SALARY)
        assertContains(errorsFor(valid.copy(salary = "-5")), FormField.SALARY)
    }

    @Test
    fun `an empty form reports every field at once`() {
        assertEquals(FormField.entries.size, errorsFor(EmployeeFormState()).size)
    }
}
