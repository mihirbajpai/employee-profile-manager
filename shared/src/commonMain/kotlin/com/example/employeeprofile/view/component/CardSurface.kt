package com.example.employeeprofile.view.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val CORNER = 20.dp

/**
 * The rounded panel every card in the app sits on — list rows, ranked earners, department
 * totals and the detail sections. Shared so the corner radius and surface colour can't drift
 * apart between screens.
 */
@Composable
fun CardSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CORNER),
        color = MaterialTheme.colorScheme.surface,
        content = content
    )
}
