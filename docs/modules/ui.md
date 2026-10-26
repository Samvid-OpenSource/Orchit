# UI module

## Owns

All Compose screens and editor interaction surfaces for phone and tablet.

## Exposes

Contracts in `org.samvid.orchit.ui`: `ProjectListContract` with observable project list state and list/create/rename/delete/open operations, plus `EditorUiContract` with lifecycle-collectable `StateFlow<EditorUiState>` and actions for playback, scrubbing, and clip/track selection.

Root composable entry point: `OrchitApp()` in `org.samvid.orchit.ui.OrchitApp`.

## Structure

```
org.samvid.orchit.ui
├── Api.kt                    # Project list and reactive editor contracts
├── OrchitApp.kt              # Root composable (module entry point)
├── component/
│   ├── EditorToolbar.kt      # Bottom toolbar with editing tool buttons
│   ├── PreviewArea.kt        # Video preview surface with play/pause
│   ├── TimelineArea.kt       # Track/clip timeline with selection and scrubber
│   └── TimelineModels.kt     # FakeTrack, FakeClip data classes
├── fake/
│   ├── FakeEditorUiContract.kt   # In-memory reactive editor contract
│   ├── FakeProjectListContract.kt # In-memory project list contract
│   ├── FakeProjectData.kt        # Sample project list
│   └── FakeTimelineData.kt       # Sample timeline tracks
├── navigation/
│   ├── OrchitNavigation.kt   # OrchitScreen enum, OrchitNavHost
│   └── WindowSize.kt         # OrchitWindowSize enum
├── screen/
│   ├── EditorScreen.kt       # Adaptive editor skeleton (Compact/Medium/Expanded)
│   ├── HomeScreen.kt         # Reactive project list with create/rename/delete/open
│   └── SettingsScreen.kt     # Placeholder
└── theme/
    ├── Color.kt              # Orchit owl palette (dark + light color schemes)
    ├── Theme.kt              # OrchitTheme composable
    └── Type.kt               # Typography (default for now)
```

## Theme

Dark by default, follows system setting. Palette derived from the Orchit owl logo: green (#4CAF50), purple (#7E57C2), navy (#1A237E), silver (#C0C0C0) on black.

## Adaptive Layout

Uses `OrchitWindowSize` (Compact / Medium / Expanded) derived from `BoxWithConstraints`:
- **Compact** (< 600dp): Bottom `NavigationBar`, portrait editor layout
- **Medium** (600–840dp): `NavigationRail`, side-by-side preview + timeline
- **Expanded** (≥ 840dp): `NavigationRail`, preview left / timeline+toolbar right

Editor and project-list screens collect their contracts with lifecycle-aware Compose state collection. Fakes supply sample state for previews and UI development; no engine or editing implementation is connected.

## Must not touch

Media processing, persistence implementation, format ownership, audio DSP, or network access.

## Dependencies

The UI consumes public contracts and exposes `StateFlow` through its public API. Compose state collection uses AndroidX Lifecycle Compose. The engine is the composition root and may wire modules later; all app behavior remains local and offline.
