package org.samvid.orchit.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val ToolbarHeight = 56.dp

@Composable
fun EditorToolbar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().height(ToolbarHeight),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolButton(icon = Icons.Default.ContentCut, description = "Cut")
            ToolButton(icon = Icons.Default.CallSplit, description = "Split")
            ToolButton(icon = Icons.Default.Delete, description = "Delete")
            ToolButton(icon = Icons.AutoMirrored.Default.Undo, description = "Undo")
            ToolButton(icon = Icons.AutoMirrored.Default.Redo, description = "Redo")
            ToolButton(icon = Icons.Default.SlowMotionVideo, description = "Speed")
            ToolButton(icon = Icons.Default.AutoAwesome, description = "Filters")
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    description: String,
) {
    IconButton(onClick = { /* No-op in skeleton */ }) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
