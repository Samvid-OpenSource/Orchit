# Effects module

## Owns

Visual effects, transitions, filters, LUTs, and effect presets.

## Exposes

Kotlin contracts in `org.samvid.orchit.effects`. Effect and transition data contracts plus catalog and processing interfaces.

## Must not touch

Timeline ownership, audio processing, text editing, UI screens, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline.
