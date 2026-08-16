package com.example.employeeprofile.domain.algo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How many recently opened employees are remembered. */
const val DEFAULT_RECENT_CAPACITY = 5

/**
 * The employees opened most recently, newest first — an [ArrayDeque] used as a bounded queue:
 * new arrivals join the front and the oldest leaves from the back once it's full.
 *
 * Opening someone already in the list moves them to the front instead of adding them twice, so
 * the list stays a set of distinct people in recency order.
 *
 * [ids] is a flow rather than a plain getter because viewing an employee writes nothing to the
 * database. A screen watching only the employee table would never hear about it, and the row
 * would sit stale until something unrelated happened to restart the query.
 *
 * Time complexity: O(d) for [record], where d = [capacity] — a scan to find and drop any
 * existing entry. Both ends of an ArrayDeque are O(1); the scan dominates, and d is 5.
 * Space complexity: O(d), not one entry per view ever made.
 */
class RecentlyViewed(private val capacity: Int = DEFAULT_RECENT_CAPACITY) {

    private val queue = ArrayDeque<Long>()

    private val _ids = MutableStateFlow<List<Long>>(emptyList())

    /** Newest first. */
    val ids: StateFlow<List<Long>> = _ids.asStateFlow()

    val size: Int get() = queue.size

    fun record(employeeId: Long) {
        queue.remove(employeeId)
        queue.addFirst(employeeId)
        while (queue.size > capacity) queue.removeLast()
        publish()
    }

    /** Drops anyone who no longer exists — a deleted employee shouldn't linger here. */
    fun retainAll(existing: Set<Long>) {
        if (queue.retainAll(existing)) publish()
    }

    private fun publish() {
        _ids.value = queue.toList()
    }
}
