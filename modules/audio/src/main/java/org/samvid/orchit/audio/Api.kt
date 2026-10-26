package org.samvid.orchit.audio

/** Audio content referenced from local storage. */
data class AudioAsset(val uri: String, val licenseSpdx: String)
/** Audio clip placement and gain settings. */
data class AudioClip(val id: String, val asset: AudioAsset, val startUs: Long, val durationUs: Long, val volume: Float)
/** A linear or shaped gain envelope over a clip. */
data class VolumeFade(val startUs: Long, val endUs: Long, val from: Float, val to: Float)
/** Audio processing contract for timeline playback and render. */
interface AudioProcessor { fun setVolume(clipId: String, volume: Float); fun setFade(clipId: String, fade: VolumeFade); fun syncBeats(clipId: String, beatTimesUs: List<Long>) }
