# Text module

## Owns

Text overlays, bundled font references, and text animation presets.

## Exposes

Kotlin contracts in `org.samvid.orchit.text`. Text overlay/font contracts and catalog/animation interfaces.

## Must not touch

Visual effect processing, audio processing, timeline persistence, UI screens, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline.
