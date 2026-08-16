package com.example.employeeprofile.view.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.view.component.EmptyState
import com.example.employeeprofile.view.component.SearchField
import com.example.employeeprofile.view.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

/** Leaves room under the last card so the floating button never covers it. */
private val LIST_BOTTOM_PADDING = 96.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    onAddEmployee: () -> Unit,
    onEditEmployee: (employeeId: Long) -> Unit,
    onViewEmployee: (employeeId: Long) -> Unit,
    onViewTopEarners: () -> Unit,
    vm: EmployeeListViewModel = koinViewModel()
) {
    val employees by vm.employees.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Employees") },
                actions = {
                    TextButton(onClick = onViewTopEarners) { Text("Top earners") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddEmployee,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("Add employee")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchField(
                query = searchQuery,
                onQueryChange = vm::onSearchQueryChange,
                placeholder = "Search name, email or department",
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            if (employees.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isBlank()) "No employees yet" else "No matches",
                    message = if (searchQuery.isBlank()) {
                        "Add the first one and it'll show up here."
                    } else {
                        "Nothing matches \"$searchQuery\". Try a different search."
                    }
                )
                return@Column
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.medium,
                    end = Spacing.medium,
                    top = Spacing.small,
                    bottom = LIST_BOTTOM_PADDING
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                items(items = employees, key = { it.id }) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onClick = { onViewEmployee(employee.id) }
                    )
                }
            }
        }
    }
}
