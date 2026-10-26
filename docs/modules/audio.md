# Audio module

## Owns

Audio tracks, sound effects, volume, fades, and beat synchronization.

## Exposes

Kotlin contracts in `org.samvid.orchit.audio`. Audio asset/clip/fade contracts and audio processing interface.

## Must not touch

Visual effect processing, timeline/project persistence, text overlays, UI screens, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline.
