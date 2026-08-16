package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.data.model.Employee

/** Suggestions offered while typing; more than a handful is noise rather than help. */
const val MAX_SUGGESTIONS = 5

/**
 * Prefix index over employee names, for the suggestions offered while a search is being typed.
 *
 * Every word of a name is indexed, not just the first, so "sharma" finds "Priya Sharma" — which
 * is how people actually search for a colleague.
 *
 * Time complexity: O(k) to insert a word of length k. A lookup is O(p + n) where p = length of
 * the prefix and n = the number of names underneath it — walking down to the prefix, then
 * collecting what hangs below.
 * Space complexity: O(c) where c = total characters across all indexed words.
 *
 * Names are stored at the node that ends them rather than at every node along the way. Storing
 * them at each node would make lookups O(p) flat, but at O(c × names) space — a bad trade when
 * the prefix is short and the roster isn't enormous.
 */
class NameTrie {

    private class Node {
        val children = mutableMapOf<Char, Node>()

        /** Full names whose indexed word ends here. */
        val names = mutableSetOf<String>()
    }

    private val root = Node()

    /** Rebuilds the index. O(c) over every name. */
    fun reset(employees: List<Employee>) {
        root.children.clear()
        root.names.clear()
        employees.forEach(::insert)
    }

    /** Indexes the whole name and each word of it. O(k) per word. */
    fun insert(employee: Employee) {
        val fullName = employee.fullName.trim()
        if (fullName.isEmpty()) return
        val words = fullName.split(" ").filter { it.isNotBlank() }
        (words + fullName).forEach { word -> insertWord(word.lowercase(), fullName) }
    }

    /**
     * Names starting with [prefix], at most [limit] of them, in alphabetical order so the same
     * prefix always suggests the same things.
     */
    fun suggest(prefix: String, limit: Int = MAX_SUGGESTIONS): List<String> {
        val cleaned = prefix.trim().lowercase()
        if (cleaned.isEmpty()) return emptyList()

        var node = root
        for (character in cleaned) {
            node = node.children[character] ?: return emptyList()
        }
        return collect(node).sorted().take(limit)
    }

    private fun insertWord(word: String, fullName: String) {
        var node = root
        for (character in word) {
            node = node.children.getOrPut(character) { Node() }
        }
        node.names.add(fullName)
    }

    /** Everything at or below [from]. Iterative, so a long name can't overflow the stack. */
    private fun collect(from: Node): List<String> {
        val found = mutableSetOf<String>()
        val pending = ArrayDeque<Node>()
        pending.addLast(from)
        while (pending.isNotEmpty()) {
            val node = pending.removeFirst()
            found.addAll(node.names)
            node.children.values.forEach(pending::addLast)
        }
        return found.toList()
    }
}
