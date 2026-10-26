# Effects module

## Owns

Visual effects, transitions, filters, LUTs, and effect presets.

## Exposes

Kotlin contracts in `org.samvid.orchit.effects`. Effect and transition data contracts plus catalog and processing interfaces. `TransitionPackageLoader`, `FilterPackageLoader`, and `EffectPresetLoader` validate version 1 package manifests, reject unknown fields, and require an SPDX license on every referenced asset. `CubeLutParser` reads and validates textual 3D LUT files. `EffectPackageRegistry` loads validated packages and looks them up by id and category.

## Must not touch

Timeline ownership, audio processing, text editing, UI screens, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline. Package readers use a strict JSON reader inside this module and no Android classes, so package validation runs in plain JVM unit tests.
