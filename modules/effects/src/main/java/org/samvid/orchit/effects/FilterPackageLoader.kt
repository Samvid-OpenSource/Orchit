package org.samvid.orchit.effects

/** Loads and validates version 1 filter package manifests. */
object FilterPackageLoader {
    private const val DESCRIPTION = "filter manifest"
    private val REQUIRED_FIELDS = listOf(
        "formatVersion",
        "id",
        "name",
        "lut",
        "parameters",
    )
    private val ALLOWED_FIELDS = (REQUIRED_FIELDS + "license").toSet()

    /**
     * Parses a filter manifest, rejecting unsupported versions, unknown fields, missing assets, and
     * LUT references that do not identify a local `.cube` file.
     */
    fun load(json: String): FilterPackage {
        val manifest = readManifest(json, DESCRIPTION)
        manifest.requireFields(REQUIRED_FIELDS, DESCRIPTION)
        manifest.rejectUnknownFields(ALLOWED_FIELDS, DESCRIPTION)
        val formatVersion = readFormatVersion(manifest, DESCRIPTION)

        val lut = readAsset(manifest.requiredField("lut", DESCRIPTION), "$DESCRIPTION lut asset")
        if (!lut.path.endsWith(".cube", ignoreCase = true)) {
            throw EffectFormatException("$DESCRIPTION lut asset path must identify a .cube file: ${lut.path}")
        }

        return FilterPackage(
            formatVersion = formatVersion,
            id = manifest.requiredField("id", DESCRIPTION).asNonBlankString("$DESCRIPTION id"),
            name = manifest.requiredField("name", DESCRIPTION).asNonBlankString("$DESCRIPTION name"),
            lut = lut,
            parameters = readParameterDefinitions(
                manifest.requiredField("parameters", DESCRIPTION),
                "$DESCRIPTION parameters",
            ),
            license = manifest.fields["license"]?.asNonBlankString("$DESCRIPTION license"),
        )
    }
}
