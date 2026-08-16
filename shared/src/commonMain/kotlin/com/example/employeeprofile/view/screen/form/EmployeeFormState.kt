package com.example.employeeprofile.view.screen.form

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.domain.algo.normalizePhone

/**
 * What the form holds while it's being filled in.
 *
 * Salary and phone stay as strings rather than numbers: a half-typed value has to survive
 * recomposition, and "12" mid-way to "125000" is not something to reject or reformat under the
 * user's cursor. They're parsed once, on submit.
 */
data class EmployeeFormState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val gender: Gender? = null,
    val department: Department? = null,
    val skills: Set<Skill> = emptySet(),
    val employmentType: EmploymentType? = null,
    val isActive: Boolean = true,
    val joiningDate: Long? = null,
    val salary: String = "",
    val profileImagePath: String? = null,
    val resume: ResumeDocument? = null
)

/**
 * Builds the record to persist. Only called once the validator is happy, which is what makes
 * the non-null assertions here safe — an unselected dropdown can't reach this point.
 *
 * Email is lower-cased and text trimmed on the way in, so storage holds the tidy version while
 * the user keeps seeing whatever they typed.
 */
fun EmployeeFormState.toEmployee(existing: Employee? = null): Employee = Employee(
    id = existing?.id ?: Employee.NO_ID,
    fullName = fullName.trim(),
    email = email.trim().lowercase(),
    phone = phone.trim(),
    normalizedPhone = normalizePhone(phone),
    address = address.trim(),
    gender = requireNotNull(gender),
    department = requireNotNull(department),
    skills = skills.toList(),
    employmentType = requireNotNull(employmentType),
    isActive = isActive,
    joiningDate = requireNotNull(joiningDate),
    salary = requireNotNull(salary.toDoubleOrNull()),
    profileImagePath = profileImagePath,
    resume = resume,
    // Kept from the stored record; the repository moves updatedAt on its own.
    createdAt = existing?.createdAt ?: 0L
)

/** Fills the form from a stored record, for editing. */
fun Employee.toFormState(): EmployeeFormState = EmployeeFormState(
    fullName = fullName,
    email = email,
    phone = phone,
    address = address,
    gender = gender,
    department = department,
    skills = skills.toSet(),
    employmentType = employmentType,
    isActive = isActive,
    joiningDate = joiningDate,
    // A whole number shouldn't come back into the field as "1250000.0".
    salary = if (salary % 1.0 == 0.0) salary.toLong().toString() else salary.toString(),
    profileImagePath = profileImagePath,
    resume = resume
)
