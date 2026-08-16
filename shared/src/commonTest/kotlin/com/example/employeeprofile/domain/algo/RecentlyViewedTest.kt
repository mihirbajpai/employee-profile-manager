package com.example.employeeprofile.domain.algo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecentlyViewedTest {

    @Test
    fun `newest viewed comes first`() {
        val recent = RecentlyViewed()
        listOf(1L, 2L, 3L).forEach(recent::record)
        assertEquals(listOf(3L, 2L, 1L), recent.ids())
    }

    @Test
    fun `viewing someone again moves them to the front rather than duplicating`() {
        val recent = RecentlyViewed()
        listOf(1L, 2L, 3L, 1L).forEach(recent::record)
        assertEquals(listOf(1L, 3L, 2L), recent.ids())
    }

    @Test
    fun `the queue never grows past its capacity`() {
        val recent = RecentlyViewed(capacity = 3)
        (1L..10L).forEach(recent::record)
        assertEquals(3, recent.size)
    }

    @Test
    fun `it is the oldest that is dropped when full`() {
        val recent = RecentlyViewed(capacity = 3)
        (1L..5L).forEach(recent::record)
        assertEquals(listOf(5L, 4L, 3L), recent.ids())
    }

    @Test
    fun `the default capacity is five`() {
        val recent = RecentlyViewed()
        (1L..20L).forEach(recent::record)
        assertEquals(DEFAULT_RECENT_CAPACITY, recent.size)
    }

    /** A deleted employee shouldn't keep appearing in the row. */
    @Test
    fun `retainAll drops anyone who no longer exists`() {
        val recent = RecentlyViewed()
        listOf(1L, 2L, 3L).forEach(recent::record)
        recent.retainAll(setOf(1L, 3L))
        assertEquals(listOf(3L, 1L), recent.ids())
    }

    @Test
    fun `clear empties it`() {
        val recent = RecentlyViewed()
        listOf(1L, 2L).forEach(recent::record)
        recent.clear()
        assertTrue(recent.ids().isEmpty())
    }
}
