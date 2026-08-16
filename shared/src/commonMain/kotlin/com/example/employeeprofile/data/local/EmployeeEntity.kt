package com.example.employeeprofile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
