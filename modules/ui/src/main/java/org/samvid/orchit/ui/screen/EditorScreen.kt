package org.samvid.orchit.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.samvid.orchit.ui.EditorAction
import org.samvid.orchit.ui.component.EditorToolbar
import org.samvid.orchit.ui.component.FakeTrack
import org.samvid.orchit.ui.component.PreviewArea
import org.samvid.orchit.ui.component.TimelineArea
import org.samvid.orchit.ui.fake.FakeEditorUiContract
import org.samvid.orchit.ui.fake.FakeTimelineData
import org.samvid.orchit.ui.navigation.OrchitWindowSize
import org.samvid.orchit.ui.theme.OrchitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(windowSize: OrchitWindowSize) {
    val contract = remember { FakeEditorUiContract() }
    val state = contract.currentState()
    val tracks = remember { FakeTimelineData.sampleTracks() }
    val playheadFraction = (state.playheadUs.toFloat() / 10_000_000f).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.projectName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (windowSize) {
                OrchitWindowSize.Compact -> CompactEditorLayout(
                    isPlaying = state.isPlaying,
                    tracks = tracks,
                    playheadFraction = playheadFraction,
                    onPlayPause = {
                        if (state.isPlaying) contract.dispatch(EditorAction.Pause)
                        else contract.dispatch(EditorAction.Play)
                    },
                )
                OrchitWindowSize.Medium -> MediumEditorLayout(
                    isPlaying = state.isPlaying,
                    tracks = tracks,
                    playheadFraction = playheadFraction,
                    onPlayPause = {
                        if (state.isPlaying) contract.dispatch(EditorAction.Pause)
                        else contract.dispatch(EditorAction.Play)
                    },
                )
                OrchitWindowSize.Expanded -> ExpandedEditorLayout(
                    isPlaying = state.isPlaying,
                    tracks = tracks,
                    playheadFraction = playheadFraction,
                    onPlayPause = {
                        if (state.isPlaying) contract.dispatch(EditorAction.Pause)
                        else contract.dispatch(EditorAction.Play)
                    },
                )
            }
        }
    }
}

@Composable
private fun CompactEditorLayout(
    isPlaying: Boolean,
    tracks: List<FakeTrack>,
    playheadFraction: Float,
    onPlayPause: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PreviewArea(
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPause,
            modifier = Modifier.fillMaxWidth().weight(0.4f),
        )
        TimelineArea(
            tracks = tracks,
            playheadFraction = playheadFraction,
            modifier = Modifier.fillMaxWidth().weight(0.4f),
        )
        EditorToolbar(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun MediumEditorLayout(
    isPlaying: Boolean,
    tracks: List<FakeTrack>,
    playheadFraction: Float,
    onPlayPause: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            PreviewArea(
                isPlaying = isPlaying,
                onPlayPauseClick = onPlayPause,
                modifier = Modifier.weight(0.5f).fillMaxHeight(),
            )
            TimelineArea(
                tracks = tracks,
                playheadFraction = playheadFraction,
                modifier = Modifier.weight(0.5f).fillMaxHeight(),
            )
        }
        EditorToolbar(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ExpandedEditorLayout(
    isPlaying: Boolean,
    tracks: List<FakeTrack>,
    playheadFraction: Float,
    onPlayPause: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        PreviewArea(
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPause,
            modifier = Modifier.weight(0.45f).fillMaxHeight(),
        )
        Column(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
            TimelineArea(
                tracks = tracks,
                playheadFraction = playheadFraction,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            EditorToolbar(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true, name = "Editor – Compact Dark")
@Composable
private fun EditorCompactDarkPreview() {
    OrchitTheme(darkTheme = true) {
        EditorScreen(windowSize = OrchitWindowSize.Compact)
    }
}

@Preview(showBackground = true, widthDp = 700, name = "Editor – Medium Dark")
@Composable
private fun EditorMediumDarkPreview() {
    OrchitTheme(darkTheme = true) {
        EditorScreen(windowSize = OrchitWindowSize.Medium)
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 600, name = "Editor – Expanded Dark")
@Composable
private fun EditorExpandedDarkPreview() {
    OrchitTheme(darkTheme = true) {
        EditorScreen(windowSize = OrchitWindowSize.Expanded)
    }
}
