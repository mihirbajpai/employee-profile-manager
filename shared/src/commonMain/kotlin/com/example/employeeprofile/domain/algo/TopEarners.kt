package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.data.model.Employee

/** How many top earners the section shows until the stepper changes it. */
const val DEFAULT_TOP_COUNT = 5

/** The stepper's range. */
const val MIN_TOP_COUNT = 1
const val MAX_TOP_COUNT = 10

/**
 * The [n] highest-paid employees, highest first, using a min-heap capped at [n] entries.
 *
 * Time complexity: O(m log n) where m = total employees, n = the top count
 * Space complexity: O(n) — the heap never holds more than n entries
 *
 * Why not just sort? A full sort is O(m log m) and copies the entire list. The heap keeps only
 * the best n seen so far, with the *smallest* of them at the root, so each employee costs one
 * O(1) peek to reject and only a genuine contender pays the O(log n) to replace it. With n=5
 * and m=500 that's roughly 500 comparisons plus a handful of small sifts, against 500·log₂500
 * ≈ 4500 for the sort — and 5 entries held in memory rather than 500.
 *
 * The final [sortedByDescending] runs over at most n entries, so it doesn't change the bound.
 */
fun topNBySalary(employees: List<Employee>, n: Int = DEFAULT_TOP_COUNT): List<Employee> {
    if (n <= 0 || employees.isEmpty()) return emptyList()

    val heap = MinHeap<Employee>(compareBy { it.salary })
    for (employee in employees) {
        if (heap.size < n) {
            heap.push(employee)
            continue
        }
        // The root is the weakest of the current best n — beat it, or don't bother.
        val weakest = heap.peek() ?: continue
        if (employee.salary > weakest.salary) {
            heap.poll()
            heap.push(employee)
        }
    }
    return heap.toList().sortedByDescending { it.salary }
}
