package com.example.employeeprofile.domain.algo

/** What a local number looks like once the country code is off. */
private const val LOCAL_LENGTH = 10

private const val COUNTRY_CODE = "91"

private val SEPARATORS = Regex("[\\s()\\-+]")

/**
 * Strips spaces, dashes, brackets and the leading + from a phone number, then removes a country
 * code or trunk prefix, so the same person typed two different ways compares equal.
 *
 * Time complexity: O(n) where n = length of [raw] — one pass to strip, one prefix check
 * Space complexity: O(n) for the stripped copy
 *
 * The prefix is only removed when the number is longer than a local one. Stripping it
 * unconditionally would eat the first two digits of a perfectly valid 10-digit number that
 * happens to start with 91, and leave an 8-digit number that then fails validation.
 */
fun normalizePhone(raw: String): String {
    val digits = raw.replace(SEPARATORS, "")
    if (digits.length <= LOCAL_LENGTH) return digits
    return when {
        digits.startsWith(COUNTRY_CODE) -> digits.removePrefix(COUNTRY_CODE)
        digits.startsWith("0") -> digits.removePrefix("0")
        else -> digits
    }
}
