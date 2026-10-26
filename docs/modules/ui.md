# Ui module

## Owns

All Compose screens and editor interaction surfaces for phone and tablet.

## Exposes

Kotlin contracts in `org.samvid.orchit.ui`. Editor state and user action contracts for Compose hosts.

Root composable entry point: `OrchitApp()` in `org.samvid.orchit.ui.OrchitApp`.

## Structure

```
org.samvid.orchit.ui
├── Api.kt                    # EditorUiState, EditorUiContract, EditorAction
├── OrchitApp.kt              # Root composable (module entry point)
├── component/
│   ├── EditorToolbar.kt      # Bottom toolbar with editing tool buttons
│   ├── PreviewArea.kt        # Video preview surface with play/pause
│   ├── TimelineArea.kt       # Track/clip timeline display
│   └── TimelineModels.kt     # FakeTrack, FakeClip data classes
├── fake/
│   ├── FakeEditorUiContract.kt   # In-memory EditorUiContract
│   ├── FakeProjectData.kt        # Sample project list
│   └── FakeTimelineData.kt       # Sample timeline tracks
├── navigation/
│   ├── OrchitNavigation.kt   # OrchitScreen enum, OrchitNavHost
│   └── WindowSize.kt         # OrchitWindowSize enum
├── screen/
│   ├── EditorScreen.kt       # Editor skeleton (adaptive: Compact/Medium/Expanded)
│   ├── HomeScreen.kt         # Project list with empty state
│   └── SettingsScreen.kt     # Placeholder
└── theme/
    ├── Color.kt              # Orchit owl palette (dark + light color schemes)
    ├── Theme.kt              # OrchitTheme composable
    └── Type.kt               # Typography (default for now)
```

## Theme

Dark by default, follows system setting. Palette derived from the Orchit owl logo:
green (#4CAF50), purple (#7E57C2), navy (#1A237E), silver (#C0C0C0) on black.

## Adaptive Layout

Uses `OrchitWindowSize` (Compact / Medium / Expanded) derived from `BoxWithConstraints`:
- **Compact** (< 600dp): Bottom `NavigationBar`, portrait editor layout
- **Medium** (600–840dp): `NavigationRail`, side-by-side preview + timeline
- **Expanded** (≥ 840dp): `NavigationRail`, preview left / timeline+toolbar right

## Must not touch

Media processing, persistence implementation, format ownership, audio DSP, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline.
