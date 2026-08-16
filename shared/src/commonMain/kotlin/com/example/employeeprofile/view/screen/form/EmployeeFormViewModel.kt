package com.example.employeeprofile.view.screen.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.Skill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Owns the form's fields, which of them have been visited, and what's wrong with them. */
class EmployeeFormViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmployeeFormState())
    val state: StateFlow<EmployeeFormState> = _state.asStateFlow()

    /**
     * Fields the user has finished with — left, in the case of a text field, or answered, for
     * the ones you can't focus. A field nobody has reached yet isn't shown as wrong.
     */
    private val _touched = MutableStateFlow(emptySet<FormField>())

    /** Only the errors on touched fields, so the form doesn't turn red before it's been filled. */
    val errors: StateFlow<Map<FormField, String>> = combine(_state, _touched) { state, touched ->
        EmployeeFormValidator.validate(state).filterKeys { it in touched }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), emptyMap())

    /** Gates the Submit button, and looks at every field regardless of what's been touched. */
    val isValid: StateFlow<Boolean> = _state
        .map { EmployeeFormValidator.validate(it).isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), false)

    fun onFullNameChange(value: String) = updateState { copy(fullName = value) }

    fun onEmailChange(value: String) = updateState { copy(email = value) }

    /** Anything that isn't a digit or a separator someone might type is dropped on the way in. */
    fun onPhoneChange(value: String) = updateState {
        copy(phone = value.filter { it.isDigit() || it in PHONE_SEPARATORS })
    }

    fun onAddressChange(value: String) = updateState { copy(address = value) }

    fun onGenderChange(value: Gender) {
        updateState { copy(gender = value) }
        onFieldTouched(FormField.GENDER)
    }

    fun onDepartmentChange(value: Department) {
        updateState { copy(department = value) }
        onFieldTouched(FormField.DEPARTMENT)
    }

    fun onSkillToggle(value: Skill) {
        updateState { copy(skills = if (value in skills) skills - value else skills + value) }
        onFieldTouched(FormField.SKILLS)
    }

    fun onEmploymentTypeChange(value: EmploymentType) {
        updateState { copy(employmentType = value) }
        onFieldTouched(FormField.EMPLOYMENT_TYPE)
    }

    fun onActiveChange(value: Boolean) = updateState { copy(isActive = value) }

    fun onJoiningDateChange(value: Long) {
        updateState { copy(joiningDate = value) }
        onFieldTouched(FormField.JOINING_DATE)
    }

    /** Digits and at most one decimal point; the currency symbol is the field's prefix. */
    fun onSalaryChange(value: String) = updateState {
        copy(salary = value.filter { it.isDigit() || it == '.' })
    }

    /** Called when a field loses focus, or when one that can't hold focus is answered. */
    fun onFieldTouched(field: FormField) {
        _touched.value = _touched.value + field
    }

    /** Publishes a fresh state with [block] applied. */
    private inline fun updateState(block: EmployeeFormState.() -> EmployeeFormState) {
        _state.value = _state.value.block()
    }

    private companion object {
        /** Kept as typed, then stripped when the number is normalised for storage. */
        val PHONE_SEPARATORS = setOf('+', '-', ' ', '(', ')')

        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
