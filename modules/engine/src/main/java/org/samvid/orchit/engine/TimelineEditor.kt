package org.samvid.orchit.engine

/** Immutable project editor with snapshot based undo and redo. */
class TimelineEditor(initialProject: Project) : TimelineOperations, UndoRedo {
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    /** Current immutable project snapshot. */
    override var project: Project = initialProject
        private set

    /** Adds a clip to an existing track; overlaps are allowed. */
    override fun addClip(trackId: String, clip: Clip) = edit { current ->
        require(current.tracks.none { track -> track.clips.any { it.id == clip.id } }) { "Clip id already exists" }
        current.updateTrack(trackId) { track -> track.copy(clips = (track.clips + clip).sortedBy { it.start.value }) }
    }

    /** Splits a clip at a strict interior timeline position; boundaries leave state unchanged. */
    override fun splitClip(clipId: String, at: TimelineTimeUs, newRightClipId: String): Clip? {
        val clip = findClip(clipId) ?: throw IllegalArgumentException("Unknown clip id: $clipId")
        val offset = at.value - clip.start.value
        if (offset <= 0L || offset >= clip.duration.value) return null
        require(findClip(newRightClipId) == null) { "Clip id already exists" }
        var right: Clip? = null
        edit { current ->
            current.updateClip(clipId) {
                it.copy(duration = TimelineTimeUs(offset))
            }.updateTrackContaining(clipId) { track ->
                val original = track.clips.first { it.id == clipId }
                val newClip = original.copy(
                    id = newRightClipId,
                    start = at,
                    sourceIn = TimelineTimeUs(clip.sourceIn.value + offset),
                    duration = TimelineTimeUs(clip.duration.value - offset),
                )
                right = newClip
                track.copy(clips = (track.clips + newClip).sortedBy { it.start.value })
            }
        }
        return right
    }

    /** Moves a clip to an absolute timeline position; other clips may overlap it. */
    override fun moveClip(clipId: String, newStart: TimelineTimeUs) = edit { current ->
        require(newStart.value >= 0L) { "Clip start must be non-negative" }
        current.updateClip(clipId) { it.copy(start = newStart) }
            .sortClips()
    }

    /** Trims the clip's leading edge to a timeline position, keeping at least one microsecond. */
    override fun trimStart(clipId: String, requestedStart: TimelineTimeUs) = edit { current ->
        val clip = current.clip(clipId)
        val oldEnd = clip.start.value + clip.duration.value
        val newStart = requestedStart.value.coerceIn(clip.start.value, oldEnd - 1L)
        val delta = newStart - clip.start.value
        current.updateClip(clipId) {
            it.copy(start = TimelineTimeUs(newStart), sourceIn = TimelineTimeUs(it.sourceIn.value + delta), duration = TimelineTimeUs(oldEnd - newStart))
        }
    }

    /** Trims the trailing edge to an absolute timeline end, keeping at least one microsecond. */
    override fun trimEnd(clipId: String, requestedEnd: TimelineTimeUs) = edit { current ->
        val clip = current.clip(clipId)
        val oldEnd = clip.start.value + clip.duration.value
        val newEnd = requestedEnd.value.coerceIn(clip.start.value + 1L, oldEnd)
        current.updateClip(clipId) { it.copy(duration = TimelineTimeUs(newEnd - it.start.value)) }
    }

    /** Removes a clip by id. */
    override fun deleteClip(clipId: String) = edit { current ->
        require(findClipIn(current, clipId) != null) { "Unknown clip id: $clipId" }
        current.copy(tracks = current.tracks.map { track -> track.copy(clips = track.clips.filterNot { it.id == clipId }) })
    }

    /** Appends an empty track with a unique id. */
    override fun addTrack(track: Track) = edit { current ->
        require(current.tracks.none { it.id == track.id }) { "Track id already exists" }
        require(track.clips.isEmpty()) { "New tracks must be empty" }
        current.copy(tracks = current.tracks + track)
    }

    /** Removes a track and all clips on it. */
    override fun removeTrack(trackId: String) = edit { current ->
        require(current.tracks.any { it.id == trackId }) { "Unknown track id: $trackId" }
        current.copy(tracks = current.tracks.filterNot { it.id == trackId })
    }

    /** Moves a track to the requested zero-based order index. */
    override fun reorderTrack(trackId: String, newIndex: Int) = edit { current ->
        val oldIndex = current.tracks.indexOfFirst { it.id == trackId }
        require(oldIndex >= 0) { "Unknown track id: $trackId" }
        require(newIndex in current.tracks.indices) { "Track index out of range" }
        val tracks = current.tracks.toMutableList()
        val track = tracks.removeAt(oldIndex)
        tracks.add(newIndex, track)
        current.copy(tracks = tracks)
    }

    /** Restores the most recent prior project snapshot, if one exists. */
    override fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        redoStack += project
        project = undoStack.removeAt(undoStack.lastIndex)
        return true
    }

    /** Reapplies the most recently undone project snapshot, if one exists. */
    override fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        undoStack += project
        project = redoStack.removeAt(redoStack.lastIndex)
        return true
    }

    /** Reports whether any prior snapshot is available. */
    override fun canUndo(): Boolean = undoStack.isNotEmpty()

    /** Reports whether any undone snapshot is available. */
    override fun canRedo(): Boolean = redoStack.isNotEmpty()

    private fun edit(transform: (Project) -> Project) {
        val updated = transform(project)
        if (updated != project) {
            undoStack += project
            project = updated
            redoStack.clear()
        }
    }

    private fun findClip(id: String): Clip? = findClipIn(project, id)

    private fun findClipIn(value: Project, id: String): Clip? = value.tracks.firstNotNullOfOrNull { track -> track.clips.firstOrNull { it.id == id } }

    private fun Project.clip(id: String): Clip = findClipIn(this, id) ?: throw IllegalArgumentException("Unknown clip id: $id")

    private fun Project.updateClip(id: String, transform: (Clip) -> Clip): Project {
        var found = false
        return copy(tracks = tracks.map { track ->
            track.copy(clips = track.clips.map { clip ->
                if (clip.id == id) {
                    found = true
                    transform(clip)
                } else clip
            })
        }).also { require(found) { "Unknown clip id: $id" } }
    }

    private fun Project.updateTrack(id: String, transform: (Track) -> Track): Project {
        require(tracks.any { it.id == id }) { "Unknown track id: $id" }
        return copy(tracks = tracks.map { if (it.id == id) transform(it) else it })
    }

    private fun Project.updateTrackContaining(clipId: String, transform: (Track) -> Track): Project {
        val track = tracks.firstOrNull { candidate -> candidate.clips.any { it.id == clipId } }
            ?: throw IllegalArgumentException("Unknown clip id: $clipId")
        return updateTrack(track.id, transform)
    }

    private fun Project.sortClips(): Project = copy(tracks = tracks.map { track -> track.copy(clips = track.clips.sortedBy { it.start.value }) })
}
