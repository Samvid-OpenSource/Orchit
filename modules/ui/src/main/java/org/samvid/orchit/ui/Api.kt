package org.samvid.orchit.ui

import kotlinx.coroutines.flow.StateFlow

/** Lightweight project row presented by the home screen. */
data class ProjectSummary(val id: String, val name: String)

/** Observable home screen state supplied by a project list host. */
data class ProjectListState(
    val projects: List<ProjectSummary>,
    val openedProjectId: String? = null,
)

/** Project list operations exposed to the home screen. */
interface ProjectListContract {
    /** Current project list and most recently opened project. */
    val state: StateFlow<ProjectListState>

    /** Returns the current project list. */
    fun listProjects(): List<ProjectSummary>

    /** Creates a project and returns its summary. */
    fun createProject(name: String): ProjectSummary

    /** Renames an existing project. */
    fun renameProject(projectId: String, name: String)

    /** Deletes an existing project. */
    fun deleteProject(projectId: String)

    /** Marks a project as opened and returns whether it exists. */
    fun openProject(projectId: String): Boolean
}

/** Observable state needed to present the editor shell. */
data class EditorUiState(
    val projectName: String,
    val isPlaying: Boolean,
    val playheadUs: Long,
    val selectedClipId: String? = null,
    val selectedTrackId: String? = null,
    val isScrubbing: Boolean = false,
)

/** Host contract for the Compose editor surface. */
interface EditorUiContract {
    /** Current editor state, updated as playback and user actions change it. */
    val state: StateFlow<EditorUiState>

    /** Dispatches a user intent from the editor. */
    fun dispatch(action: EditorAction)
}

/** User intent emitted by editor controls. */
sealed interface EditorAction {
    /** Starts playback. */
    data object Play : EditorAction

    /** Pauses playback. */
    data object Pause : EditorAction

    /** Seeks directly to a target timeline position. */
    data class Seek(val targetPositionUs: Long) : EditorAction

    /** Begins a drag scrub at the given target position. */
    data class ScrubStarted(val targetPositionUs: Long) : EditorAction

    /** Updates the target position during a drag scrub. */
    data class ScrubTo(val targetPositionUs: Long) : EditorAction

    /** Ends a drag scrub at the given target position. */
    data class ScrubEnded(val targetPositionUs: Long) : EditorAction

    /** Selects a timeline clip. */
    data class SelectClip(val clipId: String) : EditorAction

    /** Clears the selected clip. */
    data object DeselectClip : EditorAction

    /** Selects a timeline track. */
    data class SelectTrack(val trackId: String) : EditorAction

    /** Clears the selected track. */
    data object DeselectTrack : EditorAction
}
