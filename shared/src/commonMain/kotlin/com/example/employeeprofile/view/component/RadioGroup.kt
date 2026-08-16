package com.example.employeeprofile.view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.example.employeeprofile.view.theme.Spacing

/** Single-choice group. Wraps onto a second line rather than squeezing longer labels. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> RadioGroup(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label, error = error)
        FlowRow {
            options.forEach { option ->
                Row(
                    modifier = Modifier.selectable(
                        selected = option == selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(option) }
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = option == selected, onClick = null)
                    Text(
                        text = optionLabel(option),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = Spacing.medium)
                    )
                }
            }
        }
    }
}

/** Section label above a non-text-field control, turning red once that control is in error. */
@Composable
fun FieldLabel(text: String, error: String? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (error == null) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.error
        }
    )
    if (error != null) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
