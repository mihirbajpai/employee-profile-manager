package com.example.employeeprofile.view.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.employeeprofile.view.theme.Spacing

/**
 * A small rounded label. Filled when [container] is given — the department badge — and outlined
 * otherwise, which is how the employment type reads.
 */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    container: Color? = null,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val shaped = modifier
        .clip(CircleShape)
        .let { if (container != null) it.background(container) else it }
        .let {
            if (container == null) {
                it.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape)
            } else {
                it
            }
        }
        .padding(horizontal = Spacing.small, vertical = Spacing.xSmall)
    Text(
        text = text,
        modifier = shaped,
        style = MaterialTheme.typography.labelMedium,
        color = content
    )
}
