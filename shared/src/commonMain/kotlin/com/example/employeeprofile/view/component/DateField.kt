package com.example.employeeprofile.view.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.employeeprofile.platform.nowMillis
import com.example.employeeprofile.view.formatDate

/**
 * Read-only field that opens a date picker. Future dates are refused by the picker itself, so
 * the "not a future date" rule is enforced before it can ever be broken — the validator still
 * checks it, since an existing record could carry one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: Long?,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    onDismissed: () -> Unit = {}
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val today = nowMillis()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = value,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= today
                override fun isSelectableYear(year: Int) = true
            }
        )
        DatePickerDialog(
            onDismissRequest = {
                showPicker = false
                onDismissed()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let(onValueChange)
                        showPicker = false
                        onDismissed()
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPicker = false
                        onDismissed()
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    OutlinedTextField(
        value = value?.let(::formatDate).orEmpty(),
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    )
}
