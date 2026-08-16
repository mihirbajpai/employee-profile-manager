package com.example.employeeprofile.domain.algo

/** How many deletions can be taken back before the oldest falls off the bottom. */
const val DEFAULT_UNDO_DEPTH = 10

/**
 * Last-in, first-out history of deleted records, backed by an [ArrayDeque] used as a stack.
 *
 * Time complexity: O(1) for [push] and [pop] — an ArrayDeque adds and removes at either end
 * without shifting the rest, unlike removing from the middle of a list. Trimming past
 * [maxDepth] is O(1) too, since it drops exactly one entry from the far end per push.
 * Space complexity: O(d) where d = [maxDepth] — ten entries at most, not one per deletion ever
 * made.
 */
class UndoStack<T>(private val maxDepth: Int = DEFAULT_UNDO_DEPTH) {

    private val items = ArrayDeque<T>()

    val size: Int get() = items.size

    fun isEmpty(): Boolean = items.isEmpty()

    /** O(1). Once the stack is [maxDepth] deep, the oldest entry is discarded to make room. */
    fun push(item: T) {
        items.addLast(item)
        if (items.size > maxDepth) items.removeFirst()
    }

    /** The most recently pushed item, or null when there's nothing left to undo. O(1). */
    fun pop(): T? = items.removeLastOrNull()
}
