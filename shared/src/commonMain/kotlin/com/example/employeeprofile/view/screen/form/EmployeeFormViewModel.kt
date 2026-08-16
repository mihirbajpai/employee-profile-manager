package com.example.employeeprofile.view.screen.form

import androidx.lifecycle.ViewModel
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.Skill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Owns the form's fields. Validation and saving arrive on top of this. */
class EmployeeFormViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmployeeFormState())
    val state: StateFlow<EmployeeFormState> = _state.asStateFlow()

    fun onFullNameChange(value: String) = updateState { copy(fullName = value) }

    fun onEmailChange(value: String) = updateState { copy(email = value) }

    /** Anything that isn't a digit or a separator someone might type is dropped on the way in. */
    fun onPhoneChange(value: String) = updateState {
        copy(phone = value.filter { it.isDigit() || it in PHONE_SEPARATORS })
    }

    fun onAddressChange(value: String) = updateState { copy(address = value) }

    fun onGenderChange(value: Gender) = updateState { copy(gender = value) }

    fun onDepartmentChange(value: Department) = updateState { copy(department = value) }

    fun onSkillToggle(value: Skill) = updateState {
        copy(skills = if (value in skills) skills - value else skills + value)
    }

    fun onEmploymentTypeChange(value: EmploymentType) = updateState {
        copy(employmentType = value)
    }

    fun onActiveChange(value: Boolean) = updateState { copy(isActive = value) }

    fun onJoiningDateChange(value: Long) = updateState { copy(joiningDate = value) }

    /** Digits and at most one decimal point; the currency symbol is the field's prefix. */
    fun onSalaryChange(value: String) = updateState {
        copy(salary = value.filter { it.isDigit() || it == '.' })
    }

    /** Publishes a fresh state with [block] applied. */
    private inline fun updateState(block: EmployeeFormState.() -> EmployeeFormState) {
        _state.value = _state.value.block()
    }

    private companion object {
        /** Kept as typed, then stripped when the number is normalised for storage. */
        val PHONE_SEPARATORS = setOf('+', '-', ' ', '(', ')')
    }
}
