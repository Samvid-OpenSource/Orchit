package org.samvid.orchit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.samvid.orchit.ui.theme.Navy
import org.samvid.orchit.ui.theme.OwlGreen
import org.samvid.orchit.ui.theme.OwlPurple
import org.samvid.orchit.ui.theme.Silver

private val TrackHeight = 56.dp
private val LabelWidth = 72.dp

@Composable
fun TimelineArea(
    tracks: List<FakeTrack>,
    playheadFraction: Float,
    selectedClipId: String?,
    selectedTrackId: String?,
    onScrubStarted: (Float) -> Unit,
    onScrubTo: (Float) -> Unit,
    onScrubEnded: (Float) -> Unit,
    onSelectClip: (String) -> Unit,
    onDeselectClip: () -> Unit,
    onSelectTrack: (String) -> Unit,
    onDeselectTrack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragStarted by remember { mutableStateOf(false) }
    var latestFraction by remember(playheadFraction) { mutableStateOf(playheadFraction) }

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface) {
        Column {
            Slider(
                value = playheadFraction.coerceIn(0f, 1f),
                onValueChange = { fraction ->
                    latestFraction = fraction
                    if (!dragStarted) {
                        dragStarted = true
                        onScrubStarted(fraction)
                    }
                    onScrubTo(fraction)
                },
                onValueChangeFinished = {
                    if (dragStarted) onScrubEnded(latestFraction)
                    dragStarted = false
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            )
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    tracks.forEachIndexed { index, track ->
                        TrackRow(
                            track = track,
                            selectedTrack = selectedTrackId == track.id,
                            selectedClipId = selectedClipId,
                            onSelectClip = onSelectClip,
                            onDeselectClip = onDeselectClip,
                            onSelectTrack = onSelectTrack,
                            onDeselectTrack = onDeselectTrack,
                        )
                        if (index < tracks.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(TrackHeight * tracks.size)) {
                    val playheadX = LabelWidth + ((maxWidth - LabelWidth) * playheadFraction.coerceIn(0f, 1f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .offset(x = playheadX)
                            .background(Color(0xFFFF5252)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: FakeTrack,
    selectedTrack: Boolean,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onDeselectClip: () -> Unit,
    onSelectTrack: (String) -> Unit,
    onDeselectTrack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .background(if (selectedTrack) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = track.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (selectedTrack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .width(LabelWidth)
                .clickable {
                    if (selectedTrack) onDeselectTrack() else onSelectTrack(track.id)
                }
                .padding(start = 8.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp),
        ) {
            track.clips.forEach { clip ->
                val selected = selectedClipId == clip.id
                ClipBlock(
                    clip = clip,
                    color = colorForKind(track.kind),
                    selected = selected,
                    onClick = { if (selected) onDeselectClip() else onSelectClip(clip.id) },
                    modifier = Modifier
                        .fillMaxWidth(clip.widthFraction)
                        .fillMaxHeight()
                        .offset(x = 300.dp * clip.startFraction)
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ClipBlock(
    clip: FakeClip,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.8f))
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(6.dp))
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = clip.label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

private fun colorForKind(kind: String): Color = when (kind) {
    "video" -> OwlGreen
    "audio" -> OwlPurple
    "text" -> Navy
    else -> Silver
}
