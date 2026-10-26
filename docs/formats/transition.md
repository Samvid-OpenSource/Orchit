# Orchit transition package

A transition package contains a version 1 JSON manifest and the referenced GLSL fragment shader. `durationRangeUs` bounds allowed transition duration. Parameters define stable ids, types, defaults, and optional bounds. The optional sound reference is a local package path with an SPDX `license`. The shader receives `fromTexture`, `toTexture`, and normalized `progress`; it writes the blended frame to `outColor`. Unknown manifest fields and unsupported versions are rejected.

Schema: `schemas/transition.schema.json`. Sample manifest and shader: `samples/transition.json` and `samples/transition.frag`.
