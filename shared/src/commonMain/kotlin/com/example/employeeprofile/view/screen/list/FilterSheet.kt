package com.example.employeeprofile.view.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.employeeprofile.data.model.Department
import com.example.employeeprofile.data.model.EmploymentType
import com.example.employeeprofile.view.theme.Spacing

/** The three states of the status filter, in the order they're offered. */
private val STATUS_OPTIONS = listOf<Pair<String, Boolean?>>(
    "Any" to null,
    "Active" to true,
    "Inactive" to false
)

/**
 * The filter sheet. Nothing is applied on dismissal — each chip narrows the list as it's
 * tapped, so the sheet is a view onto the filters rather than a form to submit, and closing it
 * keeps whatever was chosen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filters: EmployeeFilters,
    onToggleDepartment: (Department) -> Unit,
    onToggleEmploymentType: (EmploymentType) -> Unit,
    onStatusChange: (Boolean?) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.xLarge)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClearAll, enabled = filters.selectionCount > 0) {
                    Text("Clear all")
                }
            }

            FilterSection(title = "Department") {
                Department.entries.forEach { department ->
                    ChoiceChip(
                        label = department.label,
                        selected = department in filters.departments,
                        onClick = { onToggleDepartment(department) }
                    )
                }
            }

            FilterSection(title = "Employment type") {
                EmploymentType.entries.forEach { type ->
                    ChoiceChip(
                        label = type.label,
                        selected = type in filters.employmentTypes,
                        onClick = { onToggleEmploymentType(type) }
                    )
                }
            }

            FilterSection(title = "Status") {
                STATUS_OPTIONS.forEach { (label, value) ->
                    ChoiceChip(
                        label = label,
                        selected = filters.isActive == value,
                        onClick = { onStatusChange(value) }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.large))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show results")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(Spacing.medium))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(Spacing.small))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}
