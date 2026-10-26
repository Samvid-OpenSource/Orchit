package org.samvid.orchit.effects

/** Version supported by every effects format reader in this module. */
internal const val EFFECT_FORMAT_VERSION = 1

/** Reads and validates a manifest root object with consistent error context. */
internal fun readManifest(json: String, description: String): JsonValue.ObjectValue =
    JsonParser.parse(json, description).asObject(description)

/** Validates the required format version and returns it. */
internal fun readFormatVersion(manifest: JsonValue.ObjectValue, description: String): Int {
    val version = manifest.requiredField("formatVersion", description)
        .asLong("$description formatVersion")
    if (version != EFFECT_FORMAT_VERSION.toLong()) {
        throw EffectFormatException(
            "Unsupported $description formatVersion $version; only version $EFFECT_FORMAT_VERSION is supported",
        )
    }
    return version.toInt()
}

/** Reads a package asset and requires its SPDX license field. */
internal fun readAsset(value: JsonValue, context: String): PackageAsset {
    val asset = value.asObject(context)
    asset.requireFields(listOf("path", "license"), context)
    asset.rejectUnknownFields(ASSET_FIELDS, context)
    return PackageAsset(
        path = asset.requiredField("path", context).asLocalPath("$context path"),
        license = asset.requiredField("license", context).asNonBlankString("$context license"),
    )
}

/** Reads a transition duration range. */
internal fun readDurationRange(value: JsonValue, context: String): DurationRange {
    val range = value.asObject(context)
    range.requireFields(listOf("min", "max"), context)
    range.rejectUnknownFields(DURATION_FIELDS, context)
    val minUs = range.requiredField("min", context).asLong("$context min")
    val maxUs = range.requiredField("max", context).asLong("$context max")
    if (minUs < 0) throw EffectFormatException("$context min must not be negative")
    if (maxUs < minUs) throw EffectFormatException("$context max must be greater than or equal to min")
    if (maxUs <= 0) throw EffectFormatException("$context max must be greater than zero")
    return DurationRange(minUs, maxUs)
}

/** Reads an ordered parameter declaration list and rejects duplicate ids. */
internal fun readParameterDefinitions(value: JsonValue, context: String): List<EffectParameterDefinition> {
    val parameters = value.asArray(context)
    val seenIds = HashSet<String>()
    return parameters.values.mapIndexed { index, element ->
        val itemContext = "$context[$index]"
        val parameter = element.asObject(itemContext)
        parameter.requireFields(PARAMETER_REQUIRED_FIELDS, itemContext)
        parameter.rejectUnknownFields(PARAMETER_FIELDS, itemContext)

        val id = parameter.requiredField("id", itemContext).asNonBlankString("$itemContext id")
        if (!seenIds.add(id)) {
            throw EffectFormatException("$context contains duplicate parameter id '$id'")
        }
        val type = parameter.requiredField("type", itemContext).asNonBlankString("$itemContext type")
        val defaultValue = parameter.requiredField("default", itemContext)
            .toEffectValue("$itemContext default")
        val minValue = parameter.fields["min"]?.asDouble("$itemContext min")
        val maxValue = parameter.fields["max"]?.asDouble("$itemContext max")
        if (minValue != null && maxValue != null && minValue > maxValue) {
            throw EffectFormatException("$itemContext min must be less than or equal to max")
        }
        if (minValue != null || maxValue != null) {
            val numericDefault = defaultValue.numericValue()
                ?: throw EffectFormatException("$itemContext default must be numeric when bounds are present")
            if (minValue != null && numericDefault < minValue) {
                throw EffectFormatException("$itemContext default must be greater than or equal to min")
            }
            if (maxValue != null && numericDefault > maxValue) {
                throw EffectFormatException("$itemContext default must be less than or equal to max")
            }
        }
        EffectParameterDefinition(id, type, defaultValue, minValue, maxValue)
    }
}

/** Reads a named effect value map. Names are effect-defined and keep their source order. */
internal fun readValueMap(value: JsonValue, context: String): Map<String, EffectValue> {
    val values = value.asObject(context)
    val result = LinkedHashMap<String, EffectValue>()
    values.fields.forEach { (name, element) ->
        if (name.isBlank()) throw EffectFormatException("$context contains a blank value name")
        result[name] = element.toEffectValue("$context.$name")
    }
    return result
}

/** Converts a JSON value into the scalar or array shape accepted by effect values. */
internal fun JsonValue.toEffectValue(context: String): EffectValue = when (this) {
    is JsonValue.StringValue -> EffectValue.Text(value)
    is JsonValue.BooleanValue -> EffectValue.Bool(value)
    is JsonValue.NumberValue -> toEffectNumber(context)
    is JsonValue.ArrayValue -> EffectValue.ArrayValue(
        values.mapIndexed { index, element -> element.toEffectValue("$context[$index]") },
    )
    is JsonValue.ObjectValue -> throw EffectFormatException(
        "$context must be a JSON scalar or array, not an object",
    )
    JsonValue.NullValue -> throw EffectFormatException(
        "$context must be a JSON scalar or array, not null",
    )
}

private fun JsonValue.NumberValue.toEffectNumber(context: String): EffectValue {
    if (integerLiteral) {
        val normalized = value.stripTrailingZeros()
        if (normalized.scale() <= 0) {
            try {
                return EffectValue.Integer(normalized.longValueExact())
            } catch (_: ArithmeticException) {
                // Values beyond the long range remain usable as decimal values.
            }
        }
    }
    return EffectValue.Decimal(asDouble(context))
}

/** Returns this value as a double when it is numeric. */
internal fun EffectValue.numericValue(): Double? = when (this) {
    is EffectValue.Integer -> value.toDouble()
    is EffectValue.Decimal -> value
    else -> null
}

/** Converts a JSON number that must be integral and nonnegative. */
internal fun JsonValue.asNonNegativeLong(context: String): Long {
    val value = asLong(context)
    if (value < 0) throw EffectFormatException("$context must not be negative")
    return value
}

private val ASSET_FIELDS = setOf("path", "license")
private val DURATION_FIELDS = setOf("min", "max")
private val PARAMETER_REQUIRED_FIELDS = listOf("id", "type", "default")
private val PARAMETER_FIELDS = setOf("id", "type", "default", "min", "max")
