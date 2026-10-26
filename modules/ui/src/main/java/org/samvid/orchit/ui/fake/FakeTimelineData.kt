package org.samvid.orchit.ui.fake

import org.samvid.orchit.ui.component.FakeClip
import org.samvid.orchit.ui.component.FakeTrack

/**
 * Provides sample timeline tracks and clips for the editor skeleton.
 */
object FakeTimelineData {

    fun sampleTracks(): List<FakeTrack> = listOf(
        FakeTrack(
            name = "Video",
            kind = "video",
            clips = listOf(
                FakeClip(label = "Intro", startFraction = 0.0f, widthFraction = 0.15f),
                FakeClip(label = "Scene 1", startFraction = 0.18f, widthFraction = 0.25f),
                FakeClip(label = "Scene 2", startFraction = 0.50f, widthFraction = 0.30f),
            ),
        ),
        FakeTrack(
            name = "Audio",
            kind = "audio",
            clips = listOf(
                FakeClip(label = "Music", startFraction = 0.0f, widthFraction = 0.60f),
                FakeClip(label = "SFX", startFraction = 0.65f, widthFraction = 0.20f),
            ),
        ),
        FakeTrack(
            name = "Text",
            kind = "text",
            clips = listOf(
                FakeClip(label = "Title", startFraction = 0.05f, widthFraction = 0.12f),
                FakeClip(label = "Credits", startFraction = 0.85f, widthFraction = 0.12f),
            ),
        ),
        FakeTrack(
            name = "FX",
            kind = "fx",
            clips = listOf(
                FakeClip(label = "Fade", startFraction = 0.43f, widthFraction = 0.07f),
            ),
        ),
    )
}
