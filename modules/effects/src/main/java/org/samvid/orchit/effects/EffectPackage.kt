package org.samvid.orchit.effects

/**
 * A scalar or array value carried by an effect parameter or preset keyframe.
 *
 * Objects and nulls are not valid effect values. Values stay close to the JSON representation so
 * effect catalogs can interpret them without the format reader guessing at effect semantics.
 */
sealed interface EffectValue {
    /** A text value. */
    data class Text(val value: String) : EffectValue
    /** An integral number value. */
    data class Integer(val value: Long) : EffectValue
    /** A non-integral number value. */
    data class Decimal(val value: Double) : EffectValue
    /** A boolean value. */
    data class Bool(val value: Boolean) : EffectValue
    /** An ordered array of effect values. */
    data class ArrayValue(val values: List<EffectValue>) : EffectValue
}

/** A local package asset and its SPDX license identifier. */
data class PackageAsset(val path: String, val license: String)

/** Inclusive bounds for a transition duration in microseconds. */
data class DurationRange(val minUs: Long, val maxUs: Long) {
    init {
        require(minUs >= 0) { "Duration minimum must not be negative" }
        require(maxUs >= minUs) { "Duration maximum must be greater than or equal to the minimum" }
    }
}

/**
 * A named parameter declared by a transition or filter package.
 *
 * [defaultValue] is the value used when an editor does not override the parameter. Bounds are only
 * present for parameters that declare them.
 */
data class EffectParameterDefinition(
    val id: String,
    val type: String,
    val defaultValue: EffectValue,
    val minValue: Double? = null,
    val maxValue: Double? = null,
)

/** A loaded, validated effects package. */
sealed interface EffectPackage {
    val id: String
    val name: String

    /** The package category when the format defines one, otherwise null. */
    val category: String?
}

/** A version 1 transition package manifest with its shader and optional sound assets. */
data class TransitionPackage(
    val formatVersion: Int,
    override val id: String,
    override val name: String,
    override val category: String,
    val shader: PackageAsset,
    val duration: DurationRange,
    val parameters: List<EffectParameterDefinition>,
    val sound: PackageAsset? = null,
) : EffectPackage

/** A version 1 filter package manifest with its `.cube` LUT asset. */
data class FilterPackage(
    val formatVersion: Int,
    override val id: String,
    override val name: String,
    val lut: PackageAsset,
    val parameters: List<EffectParameterDefinition>,
    val license: String? = null,
) : EffectPackage {
    override val category: String? get() = null
}

/** A clip-relative effect preset keyframe. */
data class PresetKeyframe(
    val timeUs: Long,
    val values: Map<String, EffectValue>,
)

/** A version 1 effect preset with its parameter map and optional keyframes. */
data class EffectPreset(
    val formatVersion: Int,
    override val id: String,
    override val name: String,
    val effectId: String,
    val parameters: Map<String, EffectValue>,
    val keyframes: List<PresetKeyframe> = emptyList(),
) : EffectPackage {
    override val category: String? get() = null
}
