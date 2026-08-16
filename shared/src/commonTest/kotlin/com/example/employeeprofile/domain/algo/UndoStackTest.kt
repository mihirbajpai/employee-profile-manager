package com.example.employeeprofile.domain.algo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UndoStackTest {

    @Test
    fun `a new stack is empty and has nothing to pop`() {
        val stack = UndoStack<String>()
        assertTrue(stack.isEmpty())
        assertNull(stack.pop())
    }

    @Test
    fun `pop returns the most recent push`() {
        val stack = UndoStack<String>()
        stack.push("first")
        stack.push("second")
        assertEquals("second", stack.pop())
        assertEquals("first", stack.pop())
    }

    @Test
    fun `the stack never grows past its depth`() {
        val stack = UndoStack<Int>(maxDepth = 3)
        repeat(10) { stack.push(it) }
        assertEquals(3, stack.size)
    }

    @Test
    fun `it is the oldest entry that falls off not the newest`() {
        val stack = UndoStack<Int>(maxDepth = 3)
        repeat(10) { stack.push(it) }
        assertEquals(listOf(9, 8, 7), generateSequence { stack.pop() }.toList())
    }

    @Test
    fun `the default depth is ten`() {
        val stack = UndoStack<Int>()
        repeat(20) { stack.push(it) }
        assertEquals(DEFAULT_UNDO_DEPTH, stack.size)
    }

}
