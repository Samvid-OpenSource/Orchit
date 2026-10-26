package org.samvid.orchit.ui.component

/** A fake clip for timeline display and selection interaction. */
data class FakeClip(
    val label: String,
    val startFraction: Float,
    val widthFraction: Float,
    val id: String = label,
)

/** A fake track for timeline display and selection interaction. */
data class FakeTrack(
    val name: String,
    val kind: String,
    val clips: List<FakeClip>,
    val id: String = name,
)
