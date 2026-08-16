package com.example.employeeprofile.view.screen.topearners

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.domain.algo.MAX_TOP_COUNT
import com.example.employeeprofile.domain.algo.MIN_TOP_COUNT
import com.example.employeeprofile.view.component.Avatar
import com.example.employeeprofile.view.component.EmptyState
import com.example.employeeprofile.view.formatSalary
import com.example.employeeprofile.view.ordinal
import com.example.employeeprofile.view.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

private val AVATAR_SIZE = 40.dp
private val RANK_WIDTH = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopEarnersScreen(
    onBack: () -> Unit,
    vm: TopEarnersViewModel = koinViewModel()
) {
    val topEarners by vm.topEarners.collectAsStateWithLifecycle()
    val topCount by vm.topCount.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Top earners") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            CountStepper(
                count = topCount,
                onChange = vm::onTopCountChange,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            if (topEarners.isEmpty()) {
                EmptyState(
                    title = "Nothing to rank yet",
                    message = "Add employees with a salary and the highest paid show up here."
                )
                return@Column
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                itemsIndexed(items = topEarners, key = { _, it -> it.id }) { index, employee ->
                    RankedCard(rank = index + 1, employee = employee)
                }
            }
        }
    }
}

/** Steps how many earners to rank, between [MIN_TOP_COUNT] and [MAX_TOP_COUNT]. */
@Composable
private fun CountStepper(count: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Showing top $count",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onChange(-1) }, enabled = count > MIN_TOP_COUNT) {
            Text("−")
        }
        Spacer(Modifier.width(Spacing.small))
        FilledTonalIconButton(onClick = { onChange(1) }, enabled = count < MAX_TOP_COUNT) {
            Text("+")
        }
    }
}

@Composable
private fun RankedCard(rank: Int, employee: Employee) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ordinal(rank),
                modifier = Modifier.width(RANK_WIDTH),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Avatar(
                fullName = employee.fullName,
                size = AVATAR_SIZE,
                imagePath = employee.profileImagePath
            )
            Spacer(Modifier.width(Spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = employee.department.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatSalary(employee.salary),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
