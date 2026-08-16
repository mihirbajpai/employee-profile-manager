package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NameTrieTest {

    private val roster = listOf(
        employee(id = 1, fullName = "Priya Sharma"),
        employee(id = 2, fullName = "Priyanka Rao"),
        employee(id = 3, fullName = "Arjun Mehta"),
        employee(id = 4, fullName = "Sharmila Nair")
    )

    private fun trie() = NameTrie().apply { reset(roster) }

    @Test
    fun `a prefix finds every name that starts with it`() {
        assertEquals(listOf("Priya Sharma", "Priyanka Rao"), trie().suggest("pri"))
    }

    @Test
    fun `matching is case-insensitive`() {
        assertEquals(trie().suggest("PRI"), trie().suggest("pri"))
    }

    /** People search by surname as readily as by first name. */
    @Test
    fun `any word of a name is searchable not just the first`() {
        assertEquals(listOf("Priya Sharma"), trie().suggest("sharma"))
    }

    @Test
    fun `a prefix shared by a first and last name finds both people`() {
        assertEquals(listOf("Priya Sharma", "Sharmila Nair"), trie().suggest("sharm"))
    }

    @Test
    fun `a prefix nobody has finds nothing`() {
        assertTrue(trie().suggest("zzz").isEmpty())
    }

    @Test
    fun `an empty prefix suggests nothing rather than everything`() {
        assertTrue(trie().suggest("").isEmpty())
        assertTrue(trie().suggest("   ").isEmpty())
    }

    @Test
    fun `results are capped`() {
        val many = (1..20).map { employee(id = it.toLong(), fullName = "Test Person $it") }
        val trie = NameTrie().apply { reset(many) }
        assertEquals(MAX_SUGGESTIONS, trie.suggest("test").size)
        assertEquals(2, trie.suggest("test", limit = 2).size)
    }

    @Test
    fun `the full name is indexed as well as its words`() {
        assertEquals(listOf("Arjun Mehta"), trie().suggest("arjun m"))
    }

    @Test
    fun `reset forgets the previous roster`() {
        val trie = trie()
        trie.reset(listOf(employee(id = 9, fullName = "Zara Khan")))
        assertTrue(trie.suggest("pri").isEmpty())
        assertEquals(listOf("Zara Khan"), trie.suggest("zar"))
    }
}
