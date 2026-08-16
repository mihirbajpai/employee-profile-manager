package com.example.employeeprofile.view.screen.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Owns the form's fields, which of them have been visited, and what's wrong with them. */
class EmployeeFormViewModel(private val repository: EmployeeRepository) : ViewModel() {

    private val _state = MutableStateFlow(EmployeeFormState())
    val state: StateFlow<EmployeeFormState> = _state.asStateFlow()

    /** The record being edited, or null when creating. Holds the id and createdAt to preserve. */
    private var editing: Employee? = null

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

    /**
     * Fills the form from storage when editing. Runs once — a second call after the user has
     * started typing would throw their edits away.
     */
    fun load(employeeId: Long) {
        if (employeeId == Employee.NO_ID || editing != null) return
        viewModelScope.launch {
            val employee = repository.findById(employeeId) ?: return@launch
            editing = employee
            _state.value = employee.toFormState()
        }
    }

    /**
     * Persists the form and calls [onSaved] once it's stored, so navigation happens after the
     * write rather than racing it. Re-validates rather than trusting [isValid], which only has
     * a current value while the screen is collecting it.
     */
    fun save(onSaved: () -> Unit) {
        if (EmployeeFormValidator.validate(_state.value).isNotEmpty()) return
        viewModelScope.launch {
            val existing = editing
            if (existing == null) {
                repository.insert(_state.value.toEmployee())
            } else {
                repository.update(_state.value.toEmployee(existing))
            }
            onSaved()
        }
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
