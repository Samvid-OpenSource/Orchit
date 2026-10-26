package org.samvid.orchit.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineEditorTest {
    @Test
    fun addSplitAndBoundarySplit() {
        val editor = editor()
        editor.addClip("v1", clip("added", 20, 10))
        assertEquals(2, editor.project.tracks.first().clips.size)
        assertNull(editor.splitClip("c1", TimelineTimeUs(0), "boundary"))
        assertNull(editor.splitClip("c1", TimelineTimeUs(10), "boundary"))
        val right = editor.splitClip("c1", TimelineTimeUs(4), "right")!!
        assertEquals(4L, editor.project.tracks.first().clips.first { it.id == "c1" }.duration.value)
        assertEquals(6L, right.duration.value)
        assertEquals(4L, right.sourceIn.value)
    }

    @Test
    fun trimsClampAtClipLengthAndPreserveOneMicrosecond() {
        val editor = editor()
        editor.trimEnd("c1", TimelineTimeUs(100))
        assertEquals(10L, editor.project.tracks.single().clips.single().duration.value)
        editor.trimEnd("c1", TimelineTimeUs(-4))
        assertEquals(1L, editor.project.tracks.single().clips.single().duration.value)

        val second = editor()
        second.trimStart("c1", TimelineTimeUs(100))
        val trimmed = second.project.tracks.single().clips.single()
        assertEquals(9L, trimmed.start.value)
        assertEquals(1L, trimmed.duration.value)
        assertEquals(9L, trimmed.sourceIn.value)
    }

    @Test
    fun moveAllowsOverlapAndSortsTimeline() {
        val editor = editor()
        editor.addClip("v1", clip("c2", 10, 5))
        editor.moveClip("c2", TimelineTimeUs(2))
        val clips = editor.project.tracks.single().clips
        assertEquals(listOf("c1", "c2"), clips.map { it.id })
        assertEquals(2L, clips[1].start.value)
    }

    @Test
    fun deleteAndTrackOperations() {
        val editor = editor()
        editor.deleteClip("c1")
        assertTrue(editor.project.tracks.single().clips.isEmpty())
        editor.addTrack(Track("a1", "audio", emptyList()))
        editor.addTrack(Track("t1", "text", emptyList()))
        editor.reorderTrack("t1", 0)
        assertEquals(listOf("t1", "v1", "a1"), editor.project.tracks.map { it.id })
        editor.removeTrack("a1")
        assertEquals(listOf("t1", "v1"), editor.project.tracks.map { it.id })
    }

    @Test
    fun undoRedoRestoresSnapshotsAndNewEditClearsRedo() {
        val editor = editor()
        editor.addClip("v1", clip("c2", 10, 5))
        editor.moveClip("c1", TimelineTimeUs(3))
        assertTrue(editor.undo())
        assertEquals(0L, editor.project.tracks.single().clips.first { it.id == "c1" }.start.value)
        assertTrue(editor.undo())
        assertEquals(1, editor.project.tracks.single().clips.size)
        assertTrue(editor.redo())
        assertEquals(2, editor.project.tracks.single().clips.size)
        editor.deleteClip("c2")
        assertFalse(editor.canRedo())
        assertTrue(editor.undo())
        assertEquals(2, editor.project.tracks.single().clips.size)
        assertFalse(editor.undo())
        assertTrue(editor.redo())
    }

    private fun editor(): TimelineEditor = TimelineEditor(
        Project(ProjectId("p1"), "Project", listOf(Track("v1", "video", listOf(clip("c1", 0, 10))))),
    )

    private fun clip(id: String, start: Long, duration: Long): Clip = Clip(
        id,
        MediaReference("local://asset", "video/mp4"),
        TimelineTimeUs(start),
        TimelineTimeUs(0),
        TimelineTimeUs(duration),
    )
}
