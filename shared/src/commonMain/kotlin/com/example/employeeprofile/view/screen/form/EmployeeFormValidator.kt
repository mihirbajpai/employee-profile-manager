package com.example.employeeprofile.view.screen.form

import com.example.employeeprofile.domain.algo.normalizePhone
import com.example.employeeprofile.platform.nowMillis

/** The form's fields, used to key errors and to remember which ones have been left. */
enum class FormField {
    FULL_NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    GENDER,
    DEPARTMENT,
    SKILLS,
    EMPLOYMENT_TYPE,
    JOINING_DATE,
    SALARY
}

private const val NAME_MIN_LENGTH = 3
private const val ADDRESS_MIN_LENGTH = 6
private const val PHONE_LENGTH = 10

/** Deliberately permissive: something@something.tld, which is what the field is checking for. */
private val EMAIL_PATTERN = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")

/**
 * Every rule the form enforces, in one place — the same pass produces the message under each
 * field and decides whether Submit is enabled, so the two can't disagree.
 *
 * An empty result means the form is valid.
 */
object EmployeeFormValidator {

    fun validate(state: EmployeeFormState): Map<FormField, String> = buildMap {
        val name = state.fullName.trim()
        when {
            name.isEmpty() -> put(FormField.FULL_NAME, "Full name is required")
            name.length < NAME_MIN_LENGTH ->
                put(FormField.FULL_NAME, "At least $NAME_MIN_LENGTH characters")
        }

        val email = state.email.trim()
        when {
            email.isEmpty() -> put(FormField.EMAIL, "Email is required")
            EMAIL_PATTERN.matches(email).not() -> put(FormField.EMAIL, "Enter a valid email")
        }

        val phone = normalizePhone(state.phone)
        when {
            phone.isEmpty() -> put(FormField.PHONE, "Phone number is required")
            phone.all { it.isDigit() }.not() -> put(FormField.PHONE, "Digits only")
            phone.length != PHONE_LENGTH -> put(FormField.PHONE, "Must be $PHONE_LENGTH digits")
        }

        val address = state.address.trim()
        when {
            address.isEmpty() -> put(FormField.ADDRESS, "Address is required")
            address.length < ADDRESS_MIN_LENGTH ->
                put(FormField.ADDRESS, "At least $ADDRESS_MIN_LENGTH characters")
        }

        if (state.gender == null) put(FormField.GENDER, "Select a gender")
        if (state.department == null) put(FormField.DEPARTMENT, "Select a department")
        if (state.skills.isEmpty()) put(FormField.SKILLS, "Select at least one skill")
        if (state.employmentType == null) {
            put(FormField.EMPLOYMENT_TYPE, "Select an employment type")
        }

        when {
            state.joiningDate == null -> put(FormField.JOINING_DATE, "Joining date is required")
            state.joiningDate > nowMillis() ->
                put(FormField.JOINING_DATE, "Can't be a future date")
        }

        val salary = state.salary.toDoubleOrNull()
        when {
            state.salary.isBlank() -> put(FormField.SALARY, "Salary is required")
            salary == null -> put(FormField.SALARY, "Enter a number")
            salary <= 0 -> put(FormField.SALARY, "Must be greater than zero")
        }
    }
}
