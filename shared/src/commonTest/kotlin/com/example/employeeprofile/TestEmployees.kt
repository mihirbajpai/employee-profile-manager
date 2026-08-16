package com.example.employeeprofile

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
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
    joiningDate: Long = 1_700_000_000_000L
): Employee = Employee(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    normalizedPhone = normalizePhone(phone),
    address = "12 MG Road, Bengaluru",
    gender = Gender.FEMALE,
    department = department,
    skills = listOf(Skill.KOTLIN),
    employmentType = EmploymentType.FULL_TIME,
    isActive = isActive,
    joiningDate = joiningDate,
    salary = salary
)
