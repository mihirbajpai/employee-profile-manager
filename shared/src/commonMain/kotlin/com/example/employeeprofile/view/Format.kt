package com.example.employeeprofile.view

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

/** Joining dates read as DD MMM YYYY, e.g. `05 Mar 2024`. */
fun formatDate(epochMillis: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return "${date.day.toString().padStart(2, '0')} ${MONTHS[date.month.ordinal]} ${date.year}"
}

/**
 * Salary with the rupee symbol and Indian digit grouping — the last three digits, then pairs,
 * so 1250000 reads as ₹12,50,000. Fractions are dropped; nobody's salary needs paise.
 */
fun formatSalary(salary: Double): String {
    val digits = salary.toLong().toString()
    if (digits.length <= 3) return "₹$digits"
    val lastThree = digits.takeLast(3)
    val rest = digits.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "₹$grouped,$lastThree"
}

/** Up to two letters for the avatar when there's no profile photo — "Priya Sharma" becomes PS. */
fun initialsOf(fullName: String): String =
    fullName.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }
