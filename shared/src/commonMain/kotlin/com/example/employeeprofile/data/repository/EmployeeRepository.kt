package com.example.employeeprofile.data.repository

import com.example.employeeprofile.data.local.EmployeeDao
import com.example.employeeprofile.data.local.toDomain
import com.example.employeeprofile.data.local.toEntity
import com.example.employeeprofile.data.model.Employee
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
class EmployeeRepository(private val dao: EmployeeDao) {

    fun observeAll(): Flow<List<Employee>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun findById(id: Long): Employee? = dao.findById(id)?.toDomain()

    /** Returns the id Room assigned. */
    suspend fun insert(employee: Employee): Long {
        val now = nowMillis()
        return dao.insert(employee.copy(createdAt = now, updatedAt = now).toEntity())
    }

    /** Keeps the original [Employee.createdAt] and moves [Employee.updatedAt] forward. */
    suspend fun update(employee: Employee) {
        dao.update(employee.copy(updatedAt = nowMillis()).toEntity())
    }

    suspend fun delete(employee: Employee) {
        dao.delete(employee.toEntity())
    }

    suspend fun count(): Int = dao.count()
}
