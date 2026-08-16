package com.example.employeeprofile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * All reads come back as a [Flow], so the list screen updates itself after any write. Search,
 * filter and sort are deliberately not SQL — they happen in the view model, where the assignment
 * wants them combined reactively.
 */
@Dao
interface EmployeeDao {

    @Query("SELECT * FROM employees ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun findById(id: Long): EmployeeEntity?

    @Insert
    suspend fun insert(employee: EmployeeEntity): Long

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Delete
    suspend fun delete(employee: EmployeeEntity)

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun count(): Int
}
