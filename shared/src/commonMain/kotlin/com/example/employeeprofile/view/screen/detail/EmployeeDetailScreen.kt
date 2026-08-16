package com.example.employeeprofile.view.screen.detail

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.view.DataState
import com.example.employeeprofile.view.component.Avatar
import com.example.employeeprofile.view.component.EmptyState
import com.example.employeeprofile.view.component.Pill
import com.example.employeeprofile.view.component.ResumeField
import com.example.employeeprofile.view.formatDate
import com.example.employeeprofile.view.formatSalary
import com.example.employeeprofile.view.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

private val HEADER_AVATAR_SIZE = 96.dp
private val STATUS_DOT_SIZE = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
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
            TopAppBar(
                title = { Text("Employee") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is DataState.Success) {
                        IconButton(onClick = { onEdit(employeeId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
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

                is DataState.Success -> EmployeeDetail(employee = current.value)
            }
        }
    }
}

@Composable
private fun EmployeeDetail(employee: Employee) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.medium)
            .padding(bottom = Spacing.xLarge)
    ) {
        Header(employee)

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
            DetailRow(label = "Status", value = if (employee.isActive) "Active" else "Inactive")
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

@Composable
private fun Header(employee: Employee) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            fullName = employee.fullName,
            size = HEADER_AVATAR_SIZE,
            imagePath = employee.profileImagePath
        )
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
            StatusChip(isActive = employee.isActive)
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val color = if (isActive) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(STATUS_DOT_SIZE)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(Spacing.xSmall))
        Text(
            text = if (isActive) "Active" else "Inactive",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) { content() }
    }
}

/** Label on the left, value on the right, wrapping onto its own line when it's long. */
@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = Spacing.xSmall)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
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
