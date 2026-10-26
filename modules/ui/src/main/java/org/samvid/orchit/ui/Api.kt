package org.samvid.orchit.ui

/** Read-only state needed to present the editor shell. */
data class EditorUiState(val projectName: String, val isPlaying: Boolean, val playheadUs: Long)
/** Host contract for the Compose editor surface. */
interface EditorUiContract { fun currentState(): EditorUiState; fun dispatch(action: EditorAction) }
/** User intent emitted by editor controls. */
sealed interface EditorAction { data object Play : EditorAction; data object Pause : EditorAction; data class Seek(val positionUs: Long) : EditorAction }
