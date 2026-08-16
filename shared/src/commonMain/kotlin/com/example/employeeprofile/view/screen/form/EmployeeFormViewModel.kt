package com.example.employeeprofile.view.screen.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.platform.PickedFile
import com.example.employeeprofile.data.repository.EmployeeRepository
import com.example.employeeprofile.domain.algo.DuplicateField
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
     * Errors that only a save attempt can find — a duplicate email, phone or name. Kept apart
     * from the field rules so editing the offending field clears it immediately.
     */
    private val _duplicateErrors = MutableStateFlow(emptyMap<FormField, String>())

    /** One-off text for the snackbar — a rejected file, or a picker that couldn't open. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /**
     * Fields the user has finished with — left, in the case of a text field, or answered, for
     * the ones you can't focus. A field nobody has reached yet isn't shown as wrong.
     */
    private val _touched = MutableStateFlow(emptySet<FormField>())

    /** Only the errors on touched fields, so the form doesn't turn red before it's been filled. */
    val errors: StateFlow<Map<FormField, String>> =
        combine(_state, _touched, _duplicateErrors) { state, touched, duplicates ->
            EmployeeFormValidator.validate(state).filterKeys { it in touched } + duplicates
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            emptyMap()
        )

    /** Gates the Submit button, and looks at every field regardless of what's been touched. */
    val isValid: StateFlow<Boolean> = _state
        .map { EmployeeFormValidator.validate(it).isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), false)

    fun onFullNameChange(value: String) {
        clearDuplicateError(FormField.FULL_NAME)
        updateState { copy(fullName = value) }
    }

    fun onEmailChange(value: String) {
        clearDuplicateError(FormField.EMAIL)
        updateState { copy(email = value) }
    }

    /** Anything that isn't a digit or a separator someone might type is dropped on the way in. */
    fun onPhoneChange(value: String) {
        clearDuplicateError(FormField.PHONE)
        updateState { copy(phone = value.filter { it.isDigit() || it in PHONE_SEPARATORS }) }
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

    /** The picker has already copied the file into app storage; only the path is stored. */
    fun onProfileImagePicked(path: String) = updateState { copy(profileImagePath = path) }

    /**
     * Attaches a picked resume. The size is checked here as well as in the picker: the picker
     * avoids copying a large file, this makes the rule hold wherever a file comes from.
     */
    fun onResumePicked(file: PickedFile) {
        if (file.sizeBytes > ResumeDocument.MAX_BYTES) {
            _message.value = ResumeDocument.TOO_LARGE_MESSAGE
            return
        }
        updateState {
            copy(
                resume = ResumeDocument(
                    path = file.path,
                    name = file.name,
                    sizeBytes = file.sizeBytes,
                    mimeType = file.mimeType
                )
            )
        }
    }

    fun onResumeRemoved() = updateState { copy(resume = null) }

    /** Reports a picker problem to the screen; cleared once the snackbar has shown it. */
    fun onPickerError(message: String) {
        _message.value = message
    }

    fun onMessageShown() {
        _message.value = null
    }

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
            val employee = _state.value.toEmployee(existing)
            // O(1) membership check against the in-memory index, before the DAO is called.
            val conflict = repository.findConflict(employee)
            if (conflict != null) {
                _duplicateErrors.value = mapOf(conflict.toFormField() to conflict.message())
                _touched.value = _touched.value + conflict.toFormField()
                return@launch
            }
            if (existing == null) repository.insert(employee) else repository.update(employee)
            onSaved()
        }
    }

    private fun clearDuplicateError(field: FormField) {
        if (field in _duplicateErrors.value) {
            _duplicateErrors.value = _duplicateErrors.value - field
        }
    }

    private fun DuplicateField.toFormField(): FormField = when (this) {
        DuplicateField.EMAIL -> FormField.EMAIL
        DuplicateField.PHONE -> FormField.PHONE
        DuplicateField.NAME -> FormField.FULL_NAME
    }

    private fun DuplicateField.message(): String = when (this) {
        DuplicateField.EMAIL -> "Another employee already uses this email"
        DuplicateField.PHONE -> "Another employee already uses this phone number"
        DuplicateField.NAME -> "An employee with this name already exists"
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
