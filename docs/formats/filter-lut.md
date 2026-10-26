# Orchit filter and LUT package

The version 1 JSON manifest identifies a `.cube` 3D LUT, package id, parameter definitions, and the LUT asset's SPDX `license`. `.cube` data follows the textual 3D LUT convention and declares `LUT_3D_SIZE` followed by red, green, blue triples. Unknown manifest fields and unsupported versions are rejected.

Schema: `schemas/filter.schema.json`. Sample manifest and LUT: `samples/filter.json` and `samples/warm.cube`.
