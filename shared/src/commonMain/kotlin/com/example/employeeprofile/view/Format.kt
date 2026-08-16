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

/** File sizes as KB or MB, whichever reads better — 1536 becomes "1.5 KB". */
fun formatFileSize(bytes: Long): String {
    val kilobytes = bytes / 1024.0
    if (kilobytes < 1024) return "${oneDecimal(kilobytes)} KB"
    return "${oneDecimal(kilobytes / 1024.0)} MB"
}

private fun oneDecimal(value: Double): String {
    val rounded = (value * 10).toLong()
    return "${rounded / 10}.${rounded % 10}"
}

/** Rank labels for the top-earners list: 1 becomes "1st", 12 becomes "12th". */
fun ordinal(rank: Int): String {
    val suffix = when {
        // 11th, 12th and 13th break the pattern the last digit would otherwise give.
        rank % 100 in 11..13 -> "th"
        rank % 10 == 1 -> "st"
        rank % 10 == 2 -> "nd"
        rank % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$rank$suffix"
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
