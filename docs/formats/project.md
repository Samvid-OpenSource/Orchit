# Orchit project format

Version 1 stores a project name/id, duration, ordered tracks and clips. Times are integer microseconds. Each clip has a local asset path and SPDX `license`, source in-point, duration, and optional effects. Effect entries include an identifier, parameter map, and optional keyframes. Track `kind` is `video`, `audio`, or `text`. Keyframe times are relative to the containing clip; project/clip timing is relative to the project start. Unknown fields and unsupported `formatVersion` values are rejected.

Schema: `schemas/project.schema.json`. Sample: `samples/project.json`.
