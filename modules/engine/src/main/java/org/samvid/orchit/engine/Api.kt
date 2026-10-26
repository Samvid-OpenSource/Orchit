package org.samvid.orchit.engine

/** Stable identifier for a locally stored project. */
@JvmInline value class ProjectId(val value: String)
/** Stable identifier for a timeline object. */
@JvmInline value class TimelineId(val value: String)
/** A media asset reference resolved from local app storage. */
data class MediaReference(val uri: String, val mimeType: String)
/** An edit timeline position or duration in microseconds. */
@JvmInline value class TimelineTimeUs(val value: Long)
/** A clip placed on a track. */
data class Clip(val id: String, val media: MediaReference, val start: TimelineTimeUs, val sourceIn: TimelineTimeUs, val duration: TimelineTimeUs)
/** An ordered timeline track containing clips. */
data class Track(val id: String, val kind: String, val clips: List<Clip>)
/** Serializable project editing state. */
data class Project(val id: ProjectId, val name: String, val tracks: List<Track>)
/** Playback state exposed to editor clients. */
interface PlaybackController { fun seek(position: TimelineTimeUs); fun play(); fun pause(); fun position(): TimelineTimeUs }
/** Renders a project to a caller-selected local destination. */
interface ProjectExporter { fun export(project: Project, destination: MediaReference) }
/** Loads and saves local project documents. */
interface ProjectRepository { fun load(id: ProjectId): Project?; fun save(project: Project) }
