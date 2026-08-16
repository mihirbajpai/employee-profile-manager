package com.example.employeeprofile.view

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HighlightTest {

    private val mark = Color.Blue

    /** Ranges of the text that came back emphasised. */
    private fun spansOf(text: String, query: String): List<Pair<Int, Int>> =
        highlight(text, query, mark).spanStyles.map { it.start to it.end }

    @Test
    fun `the text itself is never altered`() {
        assertEquals("Aisha Khan", highlight("Aisha Khan", "sha", mark).text)
    }

    @Test
    fun `the matched range is the part emphasised`() {
        assertEquals(listOf(2 to 5), spansOf("Aisha Khan", "sha"))
    }

    @Test
    fun `matching ignores case but the original casing is kept`() {
        val result = highlight("Manav Shah", "sha", mark)
        assertEquals("Manav Shah", result.text)
        assertEquals(listOf(6 to 9), result.spanStyles.map { it.start to it.end })
    }

    @Test
    fun `every occurrence is emphasised not just the first`() {
        assertEquals(listOf(0 to 2, 3 to 5), spansOf("ab ab", "ab"))
    }

    @Test
    fun `an empty query emphasises nothing`() {
        assertTrue(spansOf("Aisha Khan", "").isEmpty())
        assertTrue(spansOf("Aisha Khan", "   ").isEmpty())
    }

    @Test
    fun `a query that does not appear emphasises nothing`() {
        assertTrue(spansOf("Aisha Khan", "zzz").isEmpty())
    }

    @Test
    fun `the query is trimmed before matching`() {
        assertEquals(listOf(2 to 5), spansOf("Aisha Khan", "  sha  "))
    }

}
