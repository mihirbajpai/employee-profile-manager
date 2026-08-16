package com.example.employeeprofile.view.screen.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.view.DataState
import com.example.employeeprofile.view.component.Avatar
import com.example.employeeprofile.view.component.CardSurface
import com.example.employeeprofile.view.component.LabelledValue
import com.example.employeeprofile.view.component.EmptyState
import com.example.employeeprofile.view.component.Pill
import com.example.employeeprofile.view.component.ResumeField
import com.example.employeeprofile.view.component.ScreenTopBar
import com.example.employeeprofile.view.component.StatusIndicator
import com.example.employeeprofile.view.formatDate
import com.example.employeeprofile.view.formatSalary
import com.example.employeeprofile.view.screen.list.avatarSharedKey
import com.example.employeeprofile.view.statusLabel
import com.example.employeeprofile.view.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

private val HEADER_AVATAR_SIZE = 96.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EmployeeDetailScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    employeeId: Long,
    onEdit: (employeeId: Long) -> Unit,
    onBack: () -> Unit,
    vm: EmployeeDetailViewModel = koinViewModel()
) {
    LaunchedEffect(employeeId) { vm.load(employeeId) }
    val state by vm.employee.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(title = "Employee", onBack = onBack) {
                if (state is DataState.Success) {
                    IconButton(onClick = { onEdit(employeeId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is DataState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                is DataState.Failure -> EmptyState(
                    title = "Not found",
                    message = current.error.message ?: "That employee couldn't be loaded."
                )

                is DataState.Success -> EmployeeDetail(
                    employee = current.value,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EmployeeDetail(
    employee: Employee,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.medium)
            .padding(bottom = Spacing.xLarge)
    ) {
        Header(employee, sharedTransitionScope, animatedVisibilityScope)

        Section(title = "Contact") {
            DetailRow(label = "Email", value = employee.email)
            DetailRow(label = "Phone", value = employee.phone)
            DetailRow(label = "Address", value = employee.address)
        }

        Section(title = "Employment") {
            DetailRow(label = "Department", value = employee.department.label)
            DetailRow(label = "Employment type", value = employee.employmentType.label)
            DetailRow(label = "Joining date", value = formatDate(employee.joiningDate))
            DetailRow(label = "Salary", value = formatSalary(employee.salary))
            DetailRow(label = "Status", value = statusLabel(employee.isActive))
        }

        Section(title = "Personal") {
            DetailRow(label = "Gender", value = employee.gender.label)
            SkillRow(employee)
        }

        if (employee.resume != null) {
            Spacer(Modifier.height(Spacing.medium))
            // Read-only here: there's nothing to upload or remove from a detail view.
            ResumeField(resume = employee.resume, onUpload = {}, onRemove = {}, readOnly = true)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Header(
    employee: Employee,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        with(sharedTransitionScope) {
            Avatar(
                fullName = employee.fullName,
                size = HEADER_AVATAR_SIZE,
                imagePath = employee.profileImagePath,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = avatarSharedKey(employee.id)
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        }
        Spacer(Modifier.height(Spacing.medium))
        Text(
            text = employee.fullName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.small))
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pill(
                text = employee.department.label,
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer
            )
            StatusIndicator(isActive = employee.isActive)
        }
    }
}


@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.medium, bottom = Spacing.small)
    )
    CardSurface {
        Column(modifier = Modifier.padding(Spacing.medium)) { content() }
    }
}

/** A labelled field, ruled off from the next one. */
@Composable
private fun DetailRow(label: String, value: String) {
    LabelledValue(
        label = label,
        value = value,
        modifier = Modifier.padding(vertical = Spacing.xSmall)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillRow(employee: Employee) {
    Column(modifier = Modifier.padding(vertical = Spacing.xSmall)) {
        Text(
            text = "Skills",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xSmall))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall)) {
            employee.skills.forEach { skill -> Pill(text = skill.label) }
        }
    }
}
