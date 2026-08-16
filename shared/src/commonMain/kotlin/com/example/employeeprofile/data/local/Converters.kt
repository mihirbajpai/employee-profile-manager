package com.example.employeeprofile.data.local

import androidx.room.TypeConverter

/** Separator for the skills column. Skill names never contain it. */
private const val SKILL_SEPARATOR = ","

/** Room can't store a list, so skills go in and come out as one comma-separated column. */
class Converters {

    @TypeConverter
    fun fromSkills(skills: List<String>): String = skills.joinToString(SKILL_SEPARATOR)

    @TypeConverter
    fun toSkills(stored: String): List<String> =
        if (stored.isBlank()) emptyList() else stored.split(SKILL_SEPARATOR)
}
