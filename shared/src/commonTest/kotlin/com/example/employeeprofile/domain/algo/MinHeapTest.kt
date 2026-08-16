package com.example.employeeprofile.domain.algo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MinHeapTest {

    private fun heap() = MinHeap<Int>(compareBy { it })

    @Test
    fun `an empty heap has nothing to peek or poll`() {
        val heap = heap()
        assertEquals(0, heap.size)
        assertNull(heap.peek())
        assertNull(heap.poll())
    }

    @Test
    fun `the smallest element sits at the root whatever order it arrived in`() {
        val heap = heap()
        listOf(5, 3, 9, 1, 7).forEach(heap::push)
        assertEquals(1, heap.peek())
    }

    @Test
    fun `polling drains the heap in ascending order`() {
        val heap = heap()
        listOf(5, 3, 9, 1, 7, 2).forEach(heap::push)
        val drained = generateSequence { heap.poll() }.toList()
        assertEquals(listOf(1, 2, 3, 5, 7, 9), drained)
    }

    @Test
    fun `duplicates are kept rather than collapsed`() {
        val heap = heap()
        listOf(2, 2, 2).forEach(heap::push)
        assertEquals(3, heap.size)
        assertEquals(listOf(2, 2, 2), generateSequence { heap.poll() }.toList())
    }

}
