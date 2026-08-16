package com.example.employeeprofile.view.screen.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.data.model.ThemePreference
import com.example.employeeprofile.platform.rememberPdfExporter
import com.example.employeeprofile.view.component.ConfirmDialog
import com.example.employeeprofile.view.component.EmptyState
import com.example.employeeprofile.view.component.SearchField
import com.example.employeeprofile.view.theme.Spacing
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.viewmodel.koinViewModel

/** Leaves room under the last card so the floating button never covers it. */
private val LIST_BOTTOM_PADDING = 96.dp

/** How close to the end the list gets before the next page is asked for. */
private const val LOAD_MORE_THRESHOLD = 5

private const val LOADING_ITEM_KEY = "loading"

/** A new row slides up from a quarter of its own height. */
private const val SLIDE_IN_FRACTION = 4

/** How long the undo offer stays up, per the brief. */
private const val UNDO_TIMEOUT_MS = 5_000L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EmployeeListScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    themePreference: ThemePreference,
    onCycleTheme: () -> Unit,
    onAddEmployee: () -> Unit,
    onEditEmployee: (employeeId: Long) -> Unit,
    onViewEmployee: (employeeId: Long) -> Unit,
    onViewTopEarners: () -> Unit,
    onViewSummary: () -> Unit,
    vm: EmployeeListViewModel = koinViewModel()
) {
    val employees by vm.employees.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()
    val sort by vm.sort.collectAsStateWithLifecycle()
    val hasMore by vm.hasMore.collectAsStateWithLifecycle()
    val matchingAll by vm.matchingAll.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Asks for the next page once the end is within a screenful, rather than at the very last
    // row, so the spinner is rarely on screen long enough to be read.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore, hasMore) {
        if (shouldLoadMore && hasMore) vm.onLoadMore()
    }
    var showFilters by remember { mutableStateOf(false) }
    var contextMenuFor by remember { mutableStateOf<Employee?>(null) }
    var pendingDelete by remember { mutableStateOf<Employee?>(null) }
    val undoPrompt by vm.undoPrompt.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Exports what the list is currently showing, filters and sort included.
    val pdfExporter = rememberPdfExporter { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Material's own durations are 4s and 10s; the brief asks for 5, so the snackbar is shown
    // indefinitely and this timeout takes it away.
    LaunchedEffect(undoPrompt) {
        val employee = undoPrompt ?: return@LaunchedEffect
        val result = withTimeoutOrNull(UNDO_TIMEOUT_MS) {
            snackbarHostState.showSnackbar(
                message = "${employee.fullName} deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Indefinite
            )
        }
        if (result == SnackbarResult.ActionPerformed) vm.onUndoDelete() else vm.onUndoPromptShown()
    }

    if (showFilters) {
        FilterSheet(
            filters = filters,
            onToggleDepartment = vm::onToggleDepartment,
            onToggleEmploymentType = vm::onToggleEmploymentType,
            onStatusChange = vm::onStatusChange,
            onClearAll = vm::onClearFilters,
            onDismiss = { showFilters = false }
        )
    }

    pendingDelete?.let { employee ->
        ConfirmDialog(
            title = "Delete employee?",
            message = "${employee.fullName} will be removed from the list.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                vm.onDelete(employee)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Employees") },
                actions = {
                    ThemeAction(preference = themePreference, onClick = onCycleTheme)
                    FilterAction(
                        selectionCount = filters.selectionCount,
                        onClick = { showFilters = true }
                    )
                    OverflowMenu(
                        onViewTopEarners = onViewTopEarners,
                        onViewSummary = onViewSummary,
                        onExportPdf = { pdfExporter.export(matchingAll) }
                    )
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
            SortMenu(
                sort = sort,
                onSortChange = vm::onSortChange,
                modifier = Modifier.padding(horizontal = Spacing.small)
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
                state = listState,
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
                    // Starts hidden and flips on first composition, so a row arriving in the
                    // list animates in rather than appearing fully formed.
                    val appearance = remember {
                        MutableTransitionState(false).apply { targetState = true }
                    }
                    AnimatedVisibility(
                        visibleState = appearance,
                        enter = fadeIn() + slideInVertically { it / SLIDE_IN_FRACTION },
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.animateItem()
                    ) {
                    Box {
                        EmployeeCard(
                            employee = employee,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onClick = { onViewEmployee(employee.id) },
                            onLongClick = { contextMenuFor = employee }
                        )
                        // Anchored to the card it was opened from, so it points at the right row.
                        DropdownMenu(
                            expanded = contextMenuFor?.id == employee.id,
                            onDismissRequest = { contextMenuFor = null },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            DropdownMenuItem(
                                text = { Text("View details") },
                                onClick = {
                                    contextMenuFor = null
                                    onViewEmployee(employee.id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    contextMenuFor = null
                                    onEditEmployee(employee.id)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                },
                                onClick = {
                                    contextMenuFor = null
                                    pendingDelete = employee
                                }
                            )
                        }
                    }
                    }
                }
                if (hasMore) {
                    item(key = LOADING_ITEM_KEY) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

/** The screens that don't warrant a permanent slot in the bar. */
@Composable
private fun OverflowMenu(
    onViewTopEarners: () -> Unit,
    onViewSummary: () -> Unit,
    onExportPdf: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            DropdownMenuItem(
                text = { Text("Top earners") },
                onClick = {
                    expanded = false
                    onViewTopEarners()
                }
            )
            DropdownMenuItem(
                text = { Text("Department summary") },
                onClick = {
                    expanded = false
                    onViewSummary()
                }
            )
            DropdownMenuItem(
                text = { Text("Export as PDF") },
                onClick = {
                    expanded = false
                    onExportPdf()
                }
            )
        }
    }
}

/** Steps the theme between System, Light and Dark, showing where it currently stands. */
@Composable
private fun ThemeAction(preference: ThemePreference, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = when (preference) {
                ThemePreference.SYSTEM -> Icons.Default.BrightnessAuto
                ThemePreference.LIGHT -> Icons.Default.LightMode
                ThemePreference.DARK -> Icons.Default.DarkMode
            },
            contentDescription = "Theme: ${preference.label}",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Filter icon with the number of active selections sitting on it. */
@Composable
private fun FilterAction(selectionCount: Int, onClick: () -> Unit) {
    BadgedBox(
        badge = {
            if (selectionCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("$selectionCount")
                }
            }
        }
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
