package com.example.employeeprofile.data.model

/** The departments an employee can belong to. Drives the dropdown and the filter chips. */
enum class Department(val label: String) {
    ENGINEERING("Engineering"),
    HR("HR"),
    SALES("Sales"),
    FINANCE("Finance"),
    DESIGN("Design"),
    OPS("Ops")
}
