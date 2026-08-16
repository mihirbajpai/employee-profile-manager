package com.example.employeeprofile.view.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.employeeprofile.view.theme.Spacing

@Composable
fun EmployeeListScreen(
    onAddEmployee: () -> Unit,
    onEditEmployee: (employeeId: Long) -> Unit,
    onViewEmployee: (employeeId: Long) -> Unit,
    onViewTopEarners: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterVertically)
    ) {
        Text(
            text = "Employees",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(onClick = onAddEmployee) { Text("Add employee") }
        TextButton(onClick = onViewTopEarners) { Text("Top earners") }
        TextButton(onClick = { onViewEmployee(1L) }) { Text("View details") }
        TextButton(onClick = { onEditEmployee(1L) }) { Text("Edit") }
    }
}
