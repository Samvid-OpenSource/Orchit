package org.samvid.orchit.effects

/** Loads and validates version 1 effect presets. */
object EffectPresetLoader {
    private const val DESCRIPTION = "effect preset"
    private val REQUIRED_FIELDS = listOf(
        "formatVersion",
        "id",
        "name",
        "effectId",
        "parameters",
    )
    private val ALLOWED_FIELDS = (REQUIRED_FIELDS + "keyframes").toSet()
    private val KEYFRAME_FIELDS = setOf("timeUs", "values")
    private val KEYFRAME_REQUIRED_FIELDS = listOf("timeUs", "values")

    /**
     * Parses an effect preset, rejecting unsupported versions, unknown fields, non-scalar values,
     * and negative keyframe times.
     */
    fun load(json: String): EffectPreset {
        val preset = readManifest(json, DESCRIPTION)
        preset.requireFields(REQUIRED_FIELDS, DESCRIPTION)
        preset.rejectUnknownFields(ALLOWED_FIELDS, DESCRIPTION)
        val formatVersion = readFormatVersion(preset, DESCRIPTION)

        val keyframes = preset.fields["keyframes"]?.let {
            readKeyframes(it, "$DESCRIPTION keyframes")
        } ?: emptyList()

        return EffectPreset(
            formatVersion = formatVersion,
            id = preset.requiredField("id", DESCRIPTION).asNonBlankString("$DESCRIPTION id"),
            name = preset.requiredField("name", DESCRIPTION).asNonBlankString("$DESCRIPTION name"),
            effectId = preset.requiredField("effectId", DESCRIPTION)
                .asNonBlankString("$DESCRIPTION effectId"),
            parameters = readValueMap(
                preset.requiredField("parameters", DESCRIPTION),
                "$DESCRIPTION parameters",
            ),
            keyframes = keyframes,
        )
    }

    private fun readKeyframes(value: JsonValue, context: String): List<PresetKeyframe> =
        value.asArray(context).values.mapIndexed { index, element ->
            val itemContext = "$context[$index]"
            val keyframe = element.asObject(itemContext)
            keyframe.requireFields(KEYFRAME_REQUIRED_FIELDS, itemContext)
            keyframe.rejectUnknownFields(KEYFRAME_FIELDS, itemContext)
            PresetKeyframe(
                timeUs = keyframe.requiredField("timeUs", itemContext)
                    .asNonNegativeLong("$itemContext timeUs"),
                values = readValueMap(
                    keyframe.requiredField("values", itemContext),
                    "$itemContext values",
                ),
            )
        }
}
