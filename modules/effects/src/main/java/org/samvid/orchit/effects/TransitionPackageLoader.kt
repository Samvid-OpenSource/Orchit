package org.samvid.orchit.effects

/** Loads and validates version 1 transition package manifests. */
object TransitionPackageLoader {
    private const val DESCRIPTION = "transition manifest"
    private val REQUIRED_FIELDS = listOf(
        "formatVersion",
        "id",
        "name",
        "category",
        "shader",
        "durationRangeUs",
        "parameters",
    )
    private val ALLOWED_FIELDS = (REQUIRED_FIELDS + "sound").toSet()

    /**
     * Parses a transition manifest, rejecting unsupported versions, unknown fields, missing assets,
     * and invalid parameter or duration declarations.
     */
    fun load(json: String): TransitionPackage {
        val manifest = readManifest(json, DESCRIPTION)
        manifest.requireFields(REQUIRED_FIELDS, DESCRIPTION)
        manifest.rejectUnknownFields(ALLOWED_FIELDS, DESCRIPTION)
        val formatVersion = readFormatVersion(manifest, DESCRIPTION)

        return TransitionPackage(
            formatVersion = formatVersion,
            id = manifest.requiredField("id", DESCRIPTION).asNonBlankString("$DESCRIPTION id"),
            name = manifest.requiredField("name", DESCRIPTION).asNonBlankString("$DESCRIPTION name"),
            category = manifest.requiredField("category", DESCRIPTION)
                .asNonBlankString("$DESCRIPTION category"),
            shader = readAsset(
                manifest.requiredField("shader", DESCRIPTION),
                "$DESCRIPTION shader asset",
            ),
            duration = readDurationRange(
                manifest.requiredField("durationRangeUs", DESCRIPTION),
                "$DESCRIPTION durationRangeUs",
            ),
            parameters = readParameterDefinitions(
                manifest.requiredField("parameters", DESCRIPTION),
                "$DESCRIPTION parameters",
            ),
            sound = manifest.fields["sound"]?.let {
                readAsset(it, "$DESCRIPTION sound asset")
            },
        )
    }
}
