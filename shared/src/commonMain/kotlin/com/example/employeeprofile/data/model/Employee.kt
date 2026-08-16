package com.example.employeeprofile.data.model

/**
 * One employee record, as the rest of the app sees it. The storage shape differs — see
 * `EmployeeEntity` — but nothing above the repository deals in that.
 *
 * [normalizedPhone] is derived from [phone] on save and is what duplicate detection compares;
 * [phone] keeps whatever the user typed.
 */
data class Employee(
    val id: Long = NO_ID,
    val fullName: String,
    val email: String,
    val phone: String,
    val normalizedPhone: String,
    val address: String,
    val gender: Gender,
    val department: Department,
    val skills: List<Skill>,
    val employmentType: EmploymentType,
    val isActive: Boolean = true,
    val joiningDate: Long,
    val salary: Double,
    val profileImagePath: String? = null,
    val resume: ResumeDocument? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    companion object {
        /** What an unsaved record carries until Room assigns the real primary key. */
        const val NO_ID = 0L
    }
}
