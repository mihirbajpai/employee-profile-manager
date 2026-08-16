package com.example.employeeprofile.view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.employeeprofile.data.model.ResumeDocument
import com.example.employeeprofile.view.formatFileSize
import com.example.employeeprofile.view.theme.Spacing

/**
 * The resume slot: an upload button until something is attached, then the file with its type
 * icon, name, size and a way to take it back off.
 */
@Composable
fun ResumeField(
    resume: ResumeDocument?,
    onUpload: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = "Resume")
        Spacer(Modifier.padding(top = Spacing.xSmall))
        if (resume == null) {
            OutlinedButton(onClick = onUpload, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(Spacing.small))
                Text("Upload resume (PDF or Word)")
            }
            return@Column
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = resume.typeIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(Spacing.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resume.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatFileSize(resume.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove resume",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** PDFs get their own glyph; Word documents and anything else fall back to a generic one. */
@Composable
private fun ResumeDocument.typeIcon() =
    if (mimeType.contains("pdf", ignoreCase = true) || name.endsWith(".pdf", ignoreCase = true)) {
        Icons.Default.PictureAsPdf
    } else {
        Icons.Default.Description
    }
