package org.samvid.orchit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.samvid.orchit.ui.theme.Navy
import org.samvid.orchit.ui.theme.OwlGreen
import org.samvid.orchit.ui.theme.OwlPurple
import org.samvid.orchit.ui.theme.Silver

private val TrackHeight = 56.dp
private val LabelWidth = 56.dp

@Composable
fun TimelineArea(
    tracks: List<FakeTrack>,
    playheadFraction: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                tracks.forEachIndexed { index, track ->
                    TrackRow(track = track)
                    if (index < tracks.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            // Playhead line
            val totalHeight = TrackHeight * tracks.size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .offset(
                            x = with(LocalDensity.current) {
                                // Offset by label width + fraction of remaining space
                                // We approximate the clip area as (screen width - LabelWidth)
                                // and place the playhead at the fraction of that
                                LabelWidth + (200.dp * playheadFraction)
                            },
                        )
                        .background(Color(0xFFFF5252)),
                )
            }
        }
    }
}

@Composable
private fun TrackRow(track: FakeTrack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = track.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(LabelWidth).padding(start = 8.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp),
        ) {
            track.clips.forEach { clip ->
                ClipBlock(
                    clip = clip,
                    color = colorForKind(track.kind),
                    modifier = Modifier
                        .fillMaxWidth(clip.widthFraction)
                        .fillMaxHeight()
                        .offset(x = with(LocalDensity.current) { (300.dp * clip.startFraction) })
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.8f)),
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
