package com.example.employeeprofile.data.repository

import com.example.employeeprofile.data.local.EmployeeDao
import com.example.employeeprofile.data.local.toDomain
import com.example.employeeprofile.data.local.toEntity
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.domain.algo.DuplicateField
import com.example.employeeprofile.domain.algo.DuplicateIndex
import com.example.employeeprofile.platform.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The only way in and out of storage. Everything above this deals in [Employee] — entities and
 * Room types stop here.
 *
 * The timestamps are stamped here rather than at the call site, so every write goes through the
 * same clock and callers can't forget.
 */
class EmployeeRepository(
    private val dao: EmployeeDao,
    private val duplicates: DuplicateIndex
) {

    /** The index is built from the database the first time anything needs it. */
    private var indexLoaded = false

    fun observeAll(): Flow<List<Employee>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun findById(id: Long): Employee? = dao.findById(id)?.toDomain()

    /**
     * Whether this record would collide with one already stored, checked in memory before the
     * database is touched at all. Returns the field at fault, or null when it's free to save.
     */
    suspend fun findConflict(employee: Employee): DuplicateField? {
        ensureIndex()
        return duplicates.findConflict(
            email = employee.email,
            normalizedPhone = employee.normalizedPhone,
            fullName = employee.fullName,
            selfId = employee.id
        )
    }

    /** Returns the id Room assigned. */
    suspend fun insert(employee: Employee): Long {
        ensureIndex()
        val now = nowMillis()
        val id = dao.insert(employee.copy(createdAt = now, updatedAt = now).toEntity())
        duplicates.add(employee.copy(id = id))
        return id
    }

    /** Keeps the original [Employee.createdAt] and moves [Employee.updatedAt] forward. */
    suspend fun update(employee: Employee) {
        ensureIndex()
        // The old email or phone has to leave the index, or it would block its own owner.
        dao.findById(employee.id)?.toDomain()?.let(duplicates::remove)
        dao.update(employee.copy(updatedAt = nowMillis()).toEntity())
        duplicates.add(employee)
    }

    /**
     * Puts a deleted record back exactly as it was — same id, same createdAt. Room's
     * autoGenerate only assigns an id when the one given is zero, so the original survives and
     * anything that referenced it still lines up.
     */
    suspend fun restore(employee: Employee) {
        ensureIndex()
        dao.insert(employee.toEntity())
        duplicates.add(employee)
    }

    suspend fun delete(employee: Employee) {
        ensureIndex()
        dao.delete(employee.toEntity())
        duplicates.remove(employee)
    }

    private suspend fun ensureIndex() {
        if (indexLoaded) return
        duplicates.reset(dao.getAll().map { it.toDomain() })
        indexLoaded = true
    }

    suspend fun count(): Int = dao.count()
}
