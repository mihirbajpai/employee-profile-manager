package com.example.employeeprofile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.data.model.Skill

/**
 * How an employee is stored. Skills go in as one comma-separated column (see [Converters]);
 * the resume is flattened into its four columns rather than an embedded type, so a record with
 * no resume costs four nulls and no extra table.
 *
 * The indices cover what the list screen filters and sorts on.
 */
@Entity(
    tableName = "employees",
    indices = [
        Index("email"),
        Index("normalizedPhone"),
        Index("department"),
        Index("employmentType"),
        Index("isActive"),
        Index("salary"),
        Index("joiningDate"),
        Index("fullName")
    ]
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val email: String,
    val phone: String,
    val normalizedPhone: String,
    val address: String,
    val gender: String,
    val department: String,
    val skills: List<String>,
    val employmentType: String,
    val isActive: Boolean,
    val joiningDate: Long,
    val salary: Double,
    val profileImagePath: String?,
    @ColumnInfo(name = "resumePath") val resumePath: String?,
    @ColumnInfo(name = "resumeName") val resumeName: String?,
    @ColumnInfo(name = "resumeSize") val resumeSize: Long?,
    @ColumnInfo(name = "resumeMimeType") val resumeMimeType: String?,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Enums are stored by name. An unrecognised name means the column outlived the enum entry, so
 * the record falls back rather than throwing and taking the whole list down with it.
 */
fun EmployeeEntity.toDomain(): Employee = Employee(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    normalizedPhone = normalizedPhone,
    address = address,
    gender = enumOrFirst(gender, Gender.entries),
    department = enumOrFirst(department, Department.entries),
    skills = skills.mapNotNull { name -> Skill.entries.firstOrNull { it.name == name } },
    employmentType = enumOrFirst(employmentType, EmploymentType.entries),
    isActive = isActive,
    joiningDate = joiningDate,
    salary = salary,
    profileImagePath = profileImagePath,
    resume = resumePath?.let {
        ResumeDocument(
            path = it,
            name = resumeName.orEmpty(),
            sizeBytes = resumeSize ?: 0L,
            mimeType = resumeMimeType.orEmpty()
        )
    },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Employee.toEntity(): EmployeeEntity = EmployeeEntity(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    normalizedPhone = normalizedPhone,
    address = address,
    gender = gender.name,
    department = department.name,
    skills = skills.map { it.name },
    employmentType = employmentType.name,
    isActive = isActive,
    joiningDate = joiningDate,
    salary = salary,
    profileImagePath = profileImagePath,
    resumePath = resume?.path,
    resumeName = resume?.name,
    resumeSize = resume?.sizeBytes,
    resumeMimeType = resume?.mimeType,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun <T : Enum<T>> enumOrFirst(stored: String, entries: List<T>): T =
    entries.firstOrNull { it.name == stored } ?: entries.first()
