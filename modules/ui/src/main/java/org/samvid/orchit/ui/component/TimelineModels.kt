package org.samvid.orchit.ui.component

/** A fake clip for timeline display. */
data class FakeClip(
    val label: String,
    val startFraction: Float,
    val widthFraction: Float,
)

/** A fake track for timeline display. */
data class FakeTrack(
    val name: String,
    val kind: String,
    val clips: List<FakeClip>,
)
