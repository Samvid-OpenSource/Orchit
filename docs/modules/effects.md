# Effects module

## Owns

Visual effects, transitions, filters, LUTs, and effect presets.

## Exposes

Kotlin contracts in `org.samvid.orchit.effects`. `EffectCatalog` exposes registered package ids, names, categories, versions, parameter definitions, primary license identifiers, and all package assets. `EffectPackageRegistry` implements the catalog over validated packages. `AppliedEffect` and `Transition` retain package id, version, and license metadata. `VisualProcessor` receives both input frames or textures and normalized transition progress.

`TransitionPackageLoader`, `FilterPackageLoader`, and `EffectPresetLoader` validate version 1 package manifests, reject unknown fields, and require an SPDX license on every referenced asset. `CubeLutParser` reads and validates textual 3D LUT files.

## Bundled packages

Starter assets live under `src/main/assets` and are loaded through the same package readers as imported assets.

- `transitions/<id>/manifest.json` and `transition.frag` provide fade, slide-left, slide-right, slide-up, slide-down, zoom-in, zoom-out, wipe, noise-dissolve, and spin. Every manifest uses the transition format's documented `blend` category, declares a normalized `progress` float, and licenses its shader under Apache-2.0.
- `filters/<id>/manifest.json` and `<id>.cube` provide warm, cool, and mono starter filters. Each manifest declares an `intensity` float and an Apache-2.0 17x17x17 3D LUT generated from an original mathematical color transform.

Shaders follow the transition format convention: they read `fromTexture` and `toTexture`, blend by normalized `progress`, and write `outColor`. Bundled assets contain only Orchit-authored designs.

## Must not touch

Timeline ownership, audio processing, text editing, UI screens, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline. Package readers use a strict JSON reader inside this module and no Android classes, so package validation runs in plain JVM unit tests.
