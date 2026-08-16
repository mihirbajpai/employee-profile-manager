package com.example.employeeprofile

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.domain.algo.normalizePhone

/**
 * A valid employee that tests vary one field at a time. Every argument has a default, so a test
 * says only what it's actually about.
 */
fun employee(
    id: Long = Employee.NO_ID,
    fullName: String = "Priya Sharma",
    email: String = "priya@acme.io",
    phone: String = "9876543210",
    salary: Double = 1_000_000.0,
    department: Department = Department.ENGINEERING,
    isActive: Boolean = true,
    joiningDate: Long = 1_700_000_000_000L,
    gender: Gender = Gender.FEMALE,
    employmentType: EmploymentType = EmploymentType.FULL_TIME,
    skills: List<Skill> = listOf(Skill.KOTLIN),
    address: String = "12 MG Road, Bengaluru",
    profileImagePath: String? = null,
    resume: ResumeDocument? = null,
    createdAt: Long = 0L,
    updatedAt: Long = 0L
): Employee = Employee(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    normalizedPhone = normalizePhone(phone),
    address = address,
    gender = gender,
    department = department,
    skills = skills,
    employmentType = employmentType,
    isActive = isActive,
    joiningDate = joiningDate,
    salary = salary,
    profileImagePath = profileImagePath,
    resume = resume,
    createdAt = createdAt,
    updatedAt = updatedAt
)
