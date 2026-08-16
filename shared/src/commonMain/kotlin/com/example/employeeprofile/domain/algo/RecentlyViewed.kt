package com.example.employeeprofile.domain.algo

/** How many recently opened employees are remembered. */
const val DEFAULT_RECENT_CAPACITY = 5

/**
 * The employees opened most recently, newest first — an [ArrayDeque] used as a bounded queue:
 * new arrivals join the front and the oldest leaves from the back once it's full.
 *
 * Opening someone already in the list moves them to the front instead of adding them twice, so
 * the list stays a set of distinct people in recency order.
 *
 * Time complexity: O(d) for [record], where d = [capacity] — a scan to find and drop any
 * existing entry. Both ends of an ArrayDeque are O(1); the scan dominates, and d is 5.
 * Space complexity: O(d), not one entry per view ever made.
 */
class RecentlyViewed(private val capacity: Int = DEFAULT_RECENT_CAPACITY) {

    private val ids = ArrayDeque<Long>()

    val size: Int get() = ids.size

    fun record(employeeId: Long) {
        ids.remove(employeeId)
        ids.addFirst(employeeId)
        while (ids.size > capacity) ids.removeLast()
    }

    /** Newest first. */
    fun ids(): List<Long> = ids.toList()

    /** Drops anyone who no longer exists — a deleted employee shouldn't linger here. */
    fun retainAll(existing: Set<Long>) {
        ids.retainAll(existing)
    }

    fun clear() = ids.clear()
}
