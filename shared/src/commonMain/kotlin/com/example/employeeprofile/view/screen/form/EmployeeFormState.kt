package com.example.employeeprofile.view.screen.form

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.data.model.Skill

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
