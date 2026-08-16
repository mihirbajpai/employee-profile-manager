package com.example.employeeprofile.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.employeeprofile.platform.ioDispatcher

@Database(entities = [EmployeeEntity::class], version = 1)
@TypeConverters(Converters::class)
@ConstructedBy(EmployeeDatabaseConstructor::class)
abstract class EmployeeDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao

    companion object {
        const val FILE_NAME = "employees.db"
    }
}

/** Room generates the `actual` for each platform; there is nothing to write by hand. */
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object EmployeeDatabaseConstructor : RoomDatabaseConstructor<EmployeeDatabase> {
    override fun initialize(): EmployeeDatabase
}

/**
 * Finishes a platform-supplied [builder]. The bundled SQLite driver ships its own engine, so
 * both platforms run the same version, and every query lands on a background dispatcher rather
 * than whichever thread called it.
 */
fun createEmployeeDatabase(builder: RoomDatabase.Builder<EmployeeDatabase>): EmployeeDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(ioDispatcher)
        .build()
