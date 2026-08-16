package com.example.employeeprofile.domain.algo

import com.example.employeeprofile.data.model.Employee

/** Which field a would-be duplicate collides on, so the form can mark the right one. */
enum class DuplicateField { EMAIL, PHONE, NAME }

/**
 * The emails, normalised phone numbers and names already taken, held in memory so saving a
 * record costs one hash lookup instead of a database round trip.
 *
 * The brief asks for two HashSets. This keeps hash *maps* from value to owning id — the same
 * O(1) lookup, but a plain set can't tell "someone else already has this email" apart from
 * "this is the record being edited", so opening an existing employee and pressing Save would
 * report them as a duplicate of themselves.
 *
 * Time complexity: O(1) average for add, remove and lookup — hash operations throughout
 * Space complexity: O(n) where n = number of employees, three entries each
 */
class DuplicateIndex {

    private val emailOwners = mutableMapOf<String, Long>()
    private val phoneOwners = mutableMapOf<String, Long>()
    private val nameOwners = mutableMapOf<String, Long>()

    /**
     * Rebuilds the index from what's in the database. Called once at start-up.
     *
     * Time complexity: O(n) — one pass over the records
     */
    fun reset(employees: List<Employee>) {
        emailOwners.clear()
        phoneOwners.clear()
        nameOwners.clear()
        employees.forEach(::add)
    }

    /** Time complexity: O(1) average */
    fun add(employee: Employee) {
        emailOwners[employee.email.lowercase()] = employee.id
        phoneOwners[employee.normalizedPhone] = employee.id
        nameOwners[employee.fullName.trim().lowercase()] = employee.id
    }

    /** Time complexity: O(1) average */
    fun remove(employee: Employee) {
        emailOwners.remove(employee.email.lowercase())
        phoneOwners.remove(employee.normalizedPhone)
        nameOwners.remove(employee.fullName.trim().lowercase())
    }

    /**
     * Reports the first field that already belongs to somebody else, or null when the record is
     * free to save. [selfId] is the record being edited, which never counts against itself.
     *
     * Time complexity: O(1) average — three hash lookups
     * Space complexity: O(1)
     */
    fun findConflict(
        email: String,
        normalizedPhone: String,
        fullName: String,
        selfId: Long = Employee.NO_ID
    ): DuplicateField? = when {
        emailOwners.takenByAnother(email.trim().lowercase(), selfId) -> DuplicateField.EMAIL
        phoneOwners.takenByAnother(normalizedPhone, selfId) -> DuplicateField.PHONE
        nameOwners.takenByAnother(fullName.trim().lowercase(), selfId) -> DuplicateField.NAME
        else -> null
    }

    private fun Map<String, Long>.takenByAnother(key: String, selfId: Long): Boolean {
        val owner = this[key] ?: return false
        return owner != selfId
    }
}
