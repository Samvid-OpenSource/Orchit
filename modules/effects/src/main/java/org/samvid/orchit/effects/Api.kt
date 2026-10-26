package org.samvid.orchit.effects

/** A named parameter value supported by an effect. */
data class EffectParameter(val name: String, val value: String)

/** Effect application bound to a timeline object and a versioned local package. */
data class AppliedEffect(
    val packageId: String,
    val packageVersion: Int,
    val license: String?,
    val effectId: String,
    val targetId: String,
    val parameters: List<EffectParameter>,
)

/** Transition between adjacent timeline objects and its source package metadata. */
data class Transition(
    val packageId: String,
    val packageVersion: Int,
    val license: String?,
    val transitionId: String,
    val durationUs: Long,
    val parameters: List<EffectParameter>,
)

/** Package metadata available to editor and engine clients. */
data class EffectPackageMetadata(
    val id: String,
    val packageVersion: Int,
    val name: String,
    val category: String?,
    val parameterDefinitions: List<EffectParameterDefinition>,
    val license: String?,
    val assets: List<PackageAsset>,
)

/** Describes locally registered effects and validates package references. */
interface EffectCatalog {
    /** IDs of all registered effect packages. */
    fun effectIds(): List<String>

    /** Metadata for every registered package. */
    fun packageMetadata(): List<EffectPackageMetadata>

    /** Metadata for a registered package id, or null when it is unknown. */
    fun findPackageMetadata(id: String): EffectPackageMetadata?

    /** Checks that the referenced package version and license match this catalog. */
    fun validate(effect: AppliedEffect): Boolean
}

/** Applies visual effects and transitions to caller-owned frame or texture values. */
interface VisualProcessor<Frame> {
    /** Applies an effect to its target through the renderer's active context. */
    fun apply(effect: AppliedEffect)

    /** Blends two frames at normalized progress for the requested transition. */
    fun transition(fromFrame: Frame, toFrame: Frame, progress: Float, transition: Transition)
}
