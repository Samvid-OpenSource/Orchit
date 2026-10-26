package org.samvid.orchit.ui.fake

import org.samvid.orchit.ui.EditorAction
import org.samvid.orchit.ui.EditorUiContract
import org.samvid.orchit.ui.EditorUiState

/**
 * In-memory fake implementation of [EditorUiContract] for UI development
 * without the engine module.
 */
class FakeEditorUiContract : EditorUiContract {

    private var state = EditorUiState(
        projectName = "My First Reel",
        isPlaying = false,
        playheadUs = 2_000_000L,
    )

    override fun currentState(): EditorUiState = state

    override fun dispatch(action: EditorAction) {
        state = when (action) {
            is EditorAction.Play -> state.copy(isPlaying = true)
            is EditorAction.Pause -> state.copy(isPlaying = false)
            is EditorAction.Seek -> state.copy(playheadUs = action.positionUs)
        }
    }
}
