package org.samvid.orchit.ui.fake

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.samvid.orchit.ui.EditorAction
import org.samvid.orchit.ui.EditorUiContract
import org.samvid.orchit.ui.EditorUiState

/** In-memory fake implementation of [EditorUiContract] for UI development without the engine. */
class FakeEditorUiContract : EditorUiContract {
    private val mutableState = MutableStateFlow(
        EditorUiState(
            projectName = "My First Reel",
            isPlaying = false,
            playheadUs = 2_000_000L,
        ),
    )

    override val state: StateFlow<EditorUiState> = mutableState

    override fun dispatch(action: EditorAction) {
        val current = mutableState.value
        mutableState.value = when (action) {
            EditorAction.Play -> current.copy(isPlaying = true)
            EditorAction.Pause -> current.copy(isPlaying = false)
            is EditorAction.Seek -> current.copy(playheadUs = action.targetPositionUs.coerceAtLeast(0L))
            is EditorAction.ScrubStarted -> current.copy(
                playheadUs = action.targetPositionUs.coerceAtLeast(0L),
                isScrubbing = true,
            )
            is EditorAction.ScrubTo -> current.copy(playheadUs = action.targetPositionUs.coerceAtLeast(0L))
            is EditorAction.ScrubEnded -> current.copy(
                playheadUs = action.targetPositionUs.coerceAtLeast(0L),
                isScrubbing = false,
            )
            is EditorAction.SelectClip -> current.copy(selectedClipId = action.clipId, selectedTrackId = null)
            EditorAction.DeselectClip -> current.copy(selectedClipId = null)
            is EditorAction.SelectTrack -> current.copy(selectedTrackId = action.trackId, selectedClipId = null)
            EditorAction.DeselectTrack -> current.copy(selectedTrackId = null)
        }
    }
}
