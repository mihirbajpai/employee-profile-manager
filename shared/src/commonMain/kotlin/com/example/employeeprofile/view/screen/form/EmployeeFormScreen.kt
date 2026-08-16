package com.example.employeeprofile.view.screen.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.data.model.Gender
import com.example.employeeprofile.data.model.Skill
import com.example.employeeprofile.platform.rememberMediaPicker
import com.example.employeeprofile.view.component.CheckboxChipGroup
import com.example.employeeprofile.view.component.DateField
import com.example.employeeprofile.view.component.LabeledDropdown
import com.example.employeeprofile.view.component.LabeledSwitch
import com.example.employeeprofile.view.component.LabeledTextField
import com.example.employeeprofile.view.component.ProfileImageField
import com.example.employeeprofile.view.component.RadioGroup
import com.example.employeeprofile.view.component.ScreenTopBar
import com.example.employeeprofile.view.component.ResumeField
import com.example.employeeprofile.view.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

/** The address field shows four lines at once, per the spec. */
private const val ADDRESS_LINES = 4

/** One form for both create and edit — [employeeId] is [Employee.NO_ID] when creating. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeFormScreen(
    employeeId: Long,
    onDone: () -> Unit,
    vm: EmployeeFormViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val errors by vm.errors.collectAsStateWithLifecycle()
    val isValid by vm.isValid.collectAsStateWithLifecycle()
    val isNew = employeeId == Employee.NO_ID
    val scope = rememberCoroutineScope()

    LaunchedEffect(employeeId) { vm.load(employeeId) }

    var showSourceSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val picker = rememberMediaPicker(
        onImagePicked = { vm.onProfileImagePicked(it.path) },
        onDocumentPicked = vm::onResumePicked,
        onError = vm::onPickerError
    )

    val message by vm.message.collectAsStateWithLifecycle()
    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        vm.onMessageShown()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = if (isNew) "New employee" else "Edit employee",
                onBack = onDone
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            ProfileImageField(
                fullName = state.fullName,
                imagePath = state.profileImagePath,
                showSourceSheet = showSourceSheet,
                onOpenSourceSheet = { showSourceSheet = true },
                onDismissSourceSheet = { showSourceSheet = false },
                onSourceChosen = { source ->
                    showSourceSheet = false
                    picker.pickImage(source)
                },
                modifier = Modifier.padding(vertical = Spacing.medium)
            )
            LabeledTextField(
                label = "Full name",
                value = state.fullName,
                onValueChange = vm::onFullNameChange,
                error = errors[FormField.FULL_NAME],
                onFocusLost = { vm.onFieldTouched(FormField.FULL_NAME) }
            )
            LabeledTextField(
                label = "Email",
                value = state.email,
                onValueChange = vm::onEmailChange,
                error = errors[FormField.EMAIL],
                keyboardType = KeyboardType.Email,
                onFocusLost = { vm.onFieldTouched(FormField.EMAIL) }
            )
            LabeledTextField(
                label = "Phone number",
                value = state.phone,
                onValueChange = vm::onPhoneChange,
                error = errors[FormField.PHONE],
                keyboardType = KeyboardType.Phone,
                onFocusLost = { vm.onFieldTouched(FormField.PHONE) }
            )
            LabeledTextField(
                label = "Address",
                value = state.address,
                onValueChange = vm::onAddressChange,
                error = errors[FormField.ADDRESS],
                singleLine = false,
                minLines = ADDRESS_LINES,
                onFocusLost = { vm.onFieldTouched(FormField.ADDRESS) }
            )
            RadioGroup(
                label = "Gender",
                options = Gender.entries,
                selected = state.gender,
                optionLabel = { it.label },
                onSelect = vm::onGenderChange,
                error = errors[FormField.GENDER]
            )
            LabeledDropdown(
                label = "Department",
                options = Department.entries,
                selected = state.department,
                optionLabel = { it.label },
                onSelect = vm::onDepartmentChange,
                error = errors[FormField.DEPARTMENT]
            )
            CheckboxChipGroup(
                label = "Skills",
                options = Skill.entries,
                selected = state.skills,
                optionLabel = { it.label },
                onToggle = vm::onSkillToggle,
                error = errors[FormField.SKILLS]
            )
            LabeledDropdown(
                label = "Employment type",
                options = EmploymentType.entries,
                selected = state.employmentType,
                optionLabel = { it.label },
                onSelect = vm::onEmploymentTypeChange,
                error = errors[FormField.EMPLOYMENT_TYPE]
            )
            LabeledSwitch(
                label = "Active",
                checked = state.isActive,
                onCheckedChange = vm::onActiveChange
            )
            DateField(
                label = "Joining date",
                value = state.joiningDate,
                onValueChange = vm::onJoiningDateChange,
                error = errors[FormField.JOINING_DATE],
                onDismissed = { vm.onFieldTouched(FormField.JOINING_DATE) }
            )
            ResumeField(
                resume = state.resume,
                onUpload = picker::pickDocument,
                onRemove = vm::onResumeRemoved
            )
            LabeledTextField(
                label = "Salary",
                value = state.salary,
                onValueChange = vm::onSalaryChange,
                error = errors[FormField.SALARY],
                keyboardType = KeyboardType.Number,
                prefix = "₹",
                onFocusLost = { vm.onFieldTouched(FormField.SALARY) }
            )
            Button(
                onClick = { vm.save(onDone) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.medium)
            ) {
                Text(if (isNew) "Create employee" else "Save changes")
            }
        }
    }
}
