package com.example.employeeprofile.view.screen.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.view.component.Avatar
import com.example.employeeprofile.view.component.CardSurface
import com.example.employeeprofile.view.component.Pill
import com.example.employeeprofile.view.component.StatusIndicator
import com.example.employeeprofile.view.formatDate
import com.example.employeeprofile.view.formatSalary
import com.example.employeeprofile.view.highlight
import com.example.employeeprofile.view.theme.Spacing

private val AVATAR_SIZE = 48.dp

/** Ties the list avatar to the detail avatar for the same employee. */
fun avatarSharedKey(employeeId: Long): String = "avatar-$employeeId"

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EmployeeCard(
    employee: Employee,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    CardSurface(modifier = modifier) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(Spacing.medium),
            verticalAlignment = Alignment.Top
        ) {
            // The avatar flies from here to the detail header and back.
            with(sharedTransitionScope) {
                Avatar(
                    fullName = employee.fullName,
                    size = AVATAR_SIZE,
                    imagePath = employee.profileImagePath,
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = avatarSharedKey(employee.id)
                        ),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
            Spacer(Modifier.width(Spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        // Shows which part of the name the search actually hit.
                        text = highlight(
                            text = employee.fullName,
                            query = searchQuery,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(Spacing.small))
                    StatusIndicator(isActive = employee.isActive)
                }
                Spacer(Modifier.height(Spacing.small))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xSmall)) {
                    Pill(
                        text = employee.department.label,
                        highlightQuery = searchQuery,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Pill(text = employee.employmentType.label)
                }
                Spacer(Modifier.height(Spacing.small))
                Text(
                    text = "${formatSalary(employee.salary)} · Joined ${formatDate(employee.joiningDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

