package com.example.employeeprofile.view.screen.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.employeeprofile.view.NEW_EMPLOYEE_ID
import com.example.employeeprofile.view.theme.Spacing

/** One form for both create and edit — [employeeId] is [NEW_EMPLOYEE_ID] when creating. */
@Composable
fun EmployeeFormScreen(employeeId: Long, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterVertically)
    ) {
        Text(
            text = if (employeeId == NEW_EMPLOYEE_ID) "New employee" else "Edit employee",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onDone) { Text("Back") }
    }
}
