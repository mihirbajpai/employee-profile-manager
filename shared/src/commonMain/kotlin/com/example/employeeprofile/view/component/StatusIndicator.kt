package com.example.employeeprofile.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.employeeprofile.view.statusLabel
import com.example.employeeprofile.view.theme.Spacing

private val DOT_SIZE = 8.dp

/**
 * A dot and a word saying whether an employee is still with the company — green when active,
 * muted when not. Shared by the list card and the detail header so the two can't drift apart.
 */
@Composable
fun StatusIndicator(isActive: Boolean, modifier: Modifier = Modifier) {
    val color = if (isActive) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(DOT_SIZE)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(Spacing.xSmall))
        Text(
            text = statusLabel(isActive),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
