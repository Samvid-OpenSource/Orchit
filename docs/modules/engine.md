# Engine module

## Owns

Project documents, timelines, clips, tracks, playback, and export orchestration.

## Exposes

Kotlin contracts in `org.samvid.orchit.engine`. Project, Track, Clip, PlaybackController, ProjectExporter, and ProjectRepository contracts. `ProjectDocument` and `ProjectJson` implement strict version 1 local project JSON parsing and writing. `TimelineEditor` provides immutable clip and track edits with snapshot undo/redo.

## Must not touch

Compose UI, effect algorithms, audio DSP, text rendering details, networking, accounts, or remote storage.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline. Project JSON uses Gson inside this module; timeline and document APIs use no Android classes.
