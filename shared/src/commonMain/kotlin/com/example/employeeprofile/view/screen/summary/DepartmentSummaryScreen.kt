package com.example.employeeprofile.view.screen.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.domain.algo.DepartmentSummary
import com.example.employeeprofile.view.component.CardSurface
import com.example.employeeprofile.view.component.LabelledValue
import com.example.employeeprofile.view.component.EmptyState
import com.example.employeeprofile.view.component.Pill
import com.example.employeeprofile.view.component.ScreenTopBar
import com.example.employeeprofile.view.formatSalary
import com.example.employeeprofile.view.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentSummaryScreen(
    onBack: () -> Unit,
    vm: DepartmentSummaryViewModel = koinViewModel()
) {
    val summaries by vm.summaries.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(title = "Department summary", onBack = onBack)
        }
    ) { padding ->
        if (summaries.isEmpty()) {
            EmptyState(
                title = "Nothing to total up",
                message = "Add employees and their departments are summarised here.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            items(items = summaries, key = { it.department.name }) { summary ->
                SummaryCard(summary)
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: DepartmentSummary) {
    CardSurface {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(
                    text = summary.department.label,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = pluralHeadcount(summary.headcount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(Spacing.small))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.small))
            Row(modifier = Modifier.fillMaxWidth()) {
                LabelledValue(
                    label = "Active",
                    value = "${summary.activeCount} of ${summary.headcount}",
                    modifier = Modifier.weight(1f)
                )
                LabelledValue(
                    label = "Average",
                    value = formatSalary(summary.averageSalary),
                    modifier = Modifier.weight(1f)
                )
                LabelledValue(
                    label = "Total",
                    value = formatSalary(summary.totalSalary),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


private fun pluralHeadcount(count: Int): String =
    if (count == 1) "1 employee" else "$count employees"
