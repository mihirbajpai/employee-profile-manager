package com.example.employeeprofile.view.screen.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.employeeprofile.data.model.Employee
import com.example.employeeprofile.view.component.Avatar
import com.example.employeeprofile.view.component.Pill
import com.example.employeeprofile.view.formatDate
import com.example.employeeprofile.view.formatSalary
import com.example.employeeprofile.view.theme.Spacing

private val AVATAR_SIZE = 48.dp

/** Ties the list avatar to the detail avatar for the same employee. */
fun avatarSharedKey(employeeId: Long): String = "avatar-$employeeId"
private val STATUS_DOT_SIZE = 8.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EmployeeCard(
    employee: Employee,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
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
                        text = employee.fullName,
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

/** A dot and a word — green when the employee is active, muted when not. */
@Composable
private fun StatusIndicator(isActive: Boolean) {
    val color = if (isActive) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(STATUS_DOT_SIZE)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(Spacing.xSmall))
        Text(
            text = if (isActive) "Active" else "Inactive",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
