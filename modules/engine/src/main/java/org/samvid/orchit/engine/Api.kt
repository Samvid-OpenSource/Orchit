package org.samvid.orchit.engine

/** Stable identifier for a locally stored project. */
@JvmInline value class ProjectId(val value: String)
/** Stable identifier for a timeline object. */
@JvmInline value class TimelineId(val value: String)
/** A media asset reference resolved from local app storage. */
data class MediaReference(val uri: String, val mimeType: String, val license: String = "")
/** An edit timeline position or duration in microseconds. */
@JvmInline value class TimelineTimeUs(val value: Long)
/** A clip placed on a track. */
data class Clip(
    val id: String,
    val media: MediaReference,
    val start: TimelineTimeUs,
    val sourceIn: TimelineTimeUs,
    val duration: TimelineTimeUs,
    val effects: List<ProjectEffectDocument> = emptyList(),
)
/** An ordered timeline track containing clips. */
data class Track(val id: String, val kind: String, val clips: List<Clip>)
/** Serializable project editing state. */
data class Project(val id: ProjectId, val name: String, val tracks: List<Track>) {
    /** Duration through the end of the latest clip, in microseconds. */
    val duration: TimelineTimeUs
        get() = TimelineTimeUs(tracks.flatMap { it.clips }.maxOfOrNull { it.start.value + it.duration.value } ?: 0L)
}

/** Versioned project document model implemented by [ProjectDocument]. */
interface ProjectDocumentContract {
    /** Current open project format version. */
    val formatVersion: Int

    /** Stable project identifier from the document. */
    val projectId: String

    /** User-visible project name. */
    val name: String

    /** Project duration in microseconds. */
    val durationUs: Long

    /** Ordered project tracks. */
    val tracks: List<ProjectTrackDocument>
}

/** Public clip and track editing operations supported by [TimelineEditor]. */
interface TimelineOperations {
    /** Current immutable project state. */
    val project: Project

    /** Adds a clip to a track. */
    fun addClip(trackId: String, clip: Clip)

    /** Splits a clip at an interior timeline position; returns null at either boundary. */
    fun splitClip(clipId: String, at: TimelineTimeUs, newRightClipId: String): Clip?

    /** Trims the leading edge to an absolute timeline position. */
    fun trimStart(clipId: String, requestedStart: TimelineTimeUs)

    /** Trims the trailing edge to an absolute timeline end. */
    fun trimEnd(clipId: String, requestedEnd: TimelineTimeUs)

    /** Moves a clip to an absolute timeline position. */
    fun moveClip(clipId: String, newStart: TimelineTimeUs)

    /** Deletes a clip by id. */
    fun deleteClip(clipId: String)

    /** Adds an empty track. */
    fun addTrack(track: Track)

    /** Removes a track and its clips. */
    fun removeTrack(trackId: String)

    /** Moves a track to a zero-based index. */
    fun reorderTrack(trackId: String, newIndex: Int)
}

/** Undo and redo controls implemented by [TimelineEditor]. */
interface UndoRedo {
    /** Restores the previous project state, when available. */
    fun undo(): Boolean

    /** Reapplies the next project state, when available. */
    fun redo(): Boolean

    /** Whether a previous project state can be restored. */
    fun canUndo(): Boolean

    /** Whether an undone project state can be reapplied. */
    fun canRedo(): Boolean
}
/** Playback state exposed to editor clients. */
interface PlaybackController { fun seek(position: TimelineTimeUs); fun play(); fun pause(); fun position(): TimelineTimeUs }
/** Renders a project to a caller-selected local destination. */
interface ProjectExporter { fun export(project: Project, destination: MediaReference) }
/** Loads and saves local project documents. */
interface ProjectRepository { fun load(id: ProjectId): Project?; fun save(project: Project) }
