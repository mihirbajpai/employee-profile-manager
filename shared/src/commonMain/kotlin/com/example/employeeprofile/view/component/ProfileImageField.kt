package com.example.employeeprofile.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.employeeprofile.platform.ImageSource
import com.example.employeeprofile.view.theme.Spacing

/** Size the brief asks for. */
private val AVATAR_SIZE = 80.dp
private val CAMERA_BADGE_SIZE = 28.dp

/**
 * The form header's profile photo: a circular avatar with a camera badge that reopens the
 * source sheet. Falls back to initials until a photo is chosen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImageField(
    fullName: String,
    imagePath: String?,
    showSourceSheet: Boolean,
    onOpenSourceSheet: () -> Unit,
    onDismissSourceSheet: () -> Unit,
    onSourceChosen: (ImageSource) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissSourceSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(bottom = Spacing.xLarge)) {
                SourceOption(
                    label = "Take photo",
                    icon = Icons.Default.PhotoCamera,
                    onClick = { onSourceChosen(ImageSource.CAMERA) }
                )
                SourceOption(
                    label = "Choose from gallery",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = { onSourceChosen(ImageSource.GALLERY) }
                )
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Avatar(fullName = fullName, size = AVATAR_SIZE, imagePath = imagePath)
            Box(
                modifier = Modifier
                    .size(CAMERA_BADGE_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onOpenSourceSheet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Change profile photo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(Spacing.medium)
                )
            }
        }
    }
}

@Composable
private fun SourceOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )
}
