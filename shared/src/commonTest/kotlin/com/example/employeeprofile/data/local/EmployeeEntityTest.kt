package com.example.employeeprofile.data.local

import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.employee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Every record in the app crosses this boundary twice. A mistake here doesn't throw — it
 * quietly changes what was saved.
 */
class EmployeeEntityTest {

    @Test
    fun `a record survives the trip to storage and back`() {
        val original = employee(
            id = 7,
            gender = Gender.MALE,
            department = Department.FINANCE,
            employmentType = EmploymentType.CONTRACT,
            skills = listOf(Skill.KOTLIN, Skill.IOS),
            isActive = false,
            createdAt = 111L,
            updatedAt = 222L
        )
        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun `enums are stored by name rather than position`() {
        val entity = employee(
            department = Department.OPS,
            gender = Gender.PREFER_NOT_TO_SAY
        ).toEntity()
        assertEquals("OPS", entity.department)
        assertEquals("PREFER_NOT_TO_SAY", entity.gender)
    }

    /** Renaming or dropping an enum entry shouldn't take the whole list down with it. */
    @Test
    fun `an unrecognised enum name falls back instead of throwing`() {
        val stored = employee().toEntity().copy(department = "MARKETING", gender = "UNKNOWN")
        val restored = stored.toDomain()
        assertEquals(Department.entries.first(), restored.department)
        assertEquals(Gender.entries.first(), restored.gender)
    }

    @Test
    fun `a skill that no longer exists is dropped rather than turned into a wrong one`() {
        val stored = employee().toEntity().copy(skills = listOf("KOTLIN", "COBOL"))
        assertEquals(listOf(Skill.KOTLIN), stored.toDomain().skills)
    }

    @Test
    fun `a resume is flattened into its columns and rebuilt`() {
        val resume = ResumeDocument("/files/cv.pdf", "cv.pdf", 2048L, "application/pdf")
        val entity = employee(resume = resume).toEntity()
        assertEquals("/files/cv.pdf", entity.resumePath)
        assertEquals("cv.pdf", entity.resumeName)
        assertEquals(2048L, entity.resumeSize)
        assertEquals(resume, entity.toDomain().resume)
    }

    @Test
    fun `no resume leaves the columns null and comes back null`() {
        val entity = employee(resume = null).toEntity()
        assertNull(entity.resumePath)
        assertNull(entity.toDomain().resume)
    }

    @Test
    fun `a profile image path is carried across untouched`() {
        val path = "/files/media/123-photo.jpg"
        assertEquals(path, employee(profileImagePath = path).toEntity().toDomain().profileImagePath)
    }

    @Test
    fun `timestamps are preserved both ways`() {
        val entity = employee(createdAt = 500L, updatedAt = 900L).toEntity()
        assertTrue(entity.createdAt == 500L && entity.updatedAt == 900L)
    }
}
