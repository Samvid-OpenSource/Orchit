# Orchit open formats

All JSON documents use UTF-8 and a required integer `formatVersion` (currently `1`). For version 1, readers must reject unknown fields at every object level and reject unsupported format versions. Producers must emit only fields defined by the corresponding schema. Paths identify files inside a package or local project storage; paths are never network URLs.

Each bundled/imported asset object carries a `license` string containing its SPDX license identifier. Asset entries must not include other license metadata fields. Examples use identifiers only and include no actual third-party assets.

## Formats

- [Project](project.md): local editing state and timeline.
- [Transition package](transition.md): manifest and GLSL fragment shader.
- [Filter/LUT package](filter-lut.md): manifest and `.cube` LUT.
- [Text animation preset](text-animation.md): timed property keyframes.
- [Effect preset](effect-preset.md): effect parameters and keyframes.

Schemas are in `schemas/`; compact valid examples and package companion files are in `samples/`.
