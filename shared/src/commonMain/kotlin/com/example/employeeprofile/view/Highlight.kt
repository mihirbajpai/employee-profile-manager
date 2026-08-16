package com.example.employeeprofile.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

/**
 * [text] with every occurrence of [query] emphasised, so a search result shows *why* it
 * matched. Case-insensitive, like the search itself.
 *
 * Returns the text unchanged when there's nothing to look for, which keeps the caller free of
 * null checks.
 */
fun highlight(text: String, query: String, color: Color): AnnotatedString {
    val needle = query.trim()
    if (needle.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            val match = text.indexOf(needle, startIndex = index, ignoreCase = true)
            if (match < 0) {
                append(text.substring(index))
                return@buildAnnotatedString
            }
            append(text.substring(index, match))
            // Taken from the original text, not the query, so "sha" doesn't lower-case "Sha".
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                append(text.substring(match, match + needle.length))
            }
            index = match + needle.length
        }
    }
}
