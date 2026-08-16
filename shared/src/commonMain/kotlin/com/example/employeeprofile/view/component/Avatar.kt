package com.example.employeeprofile.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.example.employeeprofile.view.initialsOf

/**
 * Circular avatar. Falls back to the employee's initials until there's a profile photo to show —
 * the photo path arrives with the picker work, so [imagePath] is unused for now.
 */
@Composable
fun Avatar(
    fullName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    imagePath: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(fullName),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
