package com.example.employeeprofile.domain.algo

/**
 * An array-backed binary min-heap: the smallest element by [comparator] is always at the root.
 *
 * Kotlin's common standard library has no priority queue — `java.util.PriorityQueue` is
 * JVM-only and can't be referenced from commonMain — so the classic is written out here and
 * runs identically on Android and iOS.
 *
 * Time complexity: O(log n) for [push] and [poll], O(1) for [peek] and [size]
 * Space complexity: O(n) for the backing list
 */
class MinHeap<T>(private val comparator: Comparator<T>) {

    private val items = mutableListOf<T>()

    val size: Int get() = items.size

    /** The smallest element, or null when empty. O(1). */
    fun peek(): T? = items.firstOrNull()

    /** O(log n) — appends, then walks the new element up to its place. */
    fun push(item: T) {
        items.add(item)
        siftUp(items.lastIndex)
    }

    /**
     * Removes and returns the smallest element. O(log n) — the last element moves to the root
     * and sinks back down, which keeps the tree complete without shifting the whole array.
     */
    fun poll(): T? {
        if (items.isEmpty()) return null
        val root = items[0]
        val last = items.removeAt(items.lastIndex)
        if (items.isNotEmpty()) {
            items[0] = last
            siftDown(0)
        }
        return root
    }

    /** The heap's contents in no particular order beyond the heap property. O(n). */
    fun toList(): List<T> = items.toList()

    private fun siftUp(from: Int) {
        var index = from
        while (index > 0) {
            val parent = (index - 1) / 2
            if (comparator.compare(items[index], items[parent]) >= 0) return
            swap(index, parent)
            index = parent
        }
    }

    private fun siftDown(from: Int) {
        var index = from
        while (true) {
            val left = index * 2 + 1
            val right = left + 1
            var smallest = index
            if (left < items.size && comparator.compare(items[left], items[smallest]) < 0) {
                smallest = left
            }
            if (right < items.size && comparator.compare(items[right], items[smallest]) < 0) {
                smallest = right
            }
            if (smallest == index) return
            swap(index, smallest)
            index = smallest
        }
    }

    private fun swap(a: Int, b: Int) {
        val temp = items[a]
        items[a] = items[b]
        items[b] = temp
    }
}
