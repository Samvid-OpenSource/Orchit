package org.samvid.orchit.effects

/**
 * Loads validated effects packages and indexes them by id and package category.
 *
 * The registry is an in-memory catalog. It performs no file or network access; callers supply
 * manifest and LUT text that was already read from local package storage.
 */
class EffectPackageRegistry : EffectCatalog {
    private val packagesById = LinkedHashMap<String, EffectPackage>()

    /** Number of registered packages. */
    val size: Int get() = packagesById.size

    /** Registered packages in insertion order. */
    fun packages(): List<EffectPackage> = packagesById.values.toList()

    /** IDs of all registered effect packages. */
    override fun effectIds(): List<String> = packagesById.keys.toList()

    /** Public metadata for all registered package types. */
    override fun packageMetadata(): List<EffectPackageMetadata> = packagesById.values.map(::metadata)

    /** Public metadata for a package id, or null when it is not registered. */
    override fun findPackageMetadata(id: String): EffectPackageMetadata? =
        packagesById[id]?.let(::metadata)

    /** Checks package identity, version, and declared primary asset license. */
    override fun validate(effect: AppliedEffect): Boolean {
        val metadata = findPackageMetadata(effect.packageId) ?: return false
        return metadata.packageVersion == effect.packageVersion && metadata.license == effect.license
    }

    /** Registers a validated package, rejecting duplicate ids. */
    fun register(effectPackage: EffectPackage): EffectPackage {
        if (packagesById.containsKey(effectPackage.id)) {
            throw EffectFormatException("Duplicate effect package id '${effectPackage.id}'")
        }
        packagesById[effectPackage.id] = effectPackage
        return effectPackage
    }

    /** Loads and registers a transition package manifest. */
    fun loadTransition(json: String): TransitionPackage {
        val transition = TransitionPackageLoader.load(json)
        register(transition)
        return transition
    }

    /** Loads and registers a filter package manifest. */
    fun loadFilter(json: String): FilterPackage {
        val filter = FilterPackageLoader.load(json)
        register(filter)
        return filter
    }

    /** Loads and registers an effect preset. */
    fun loadPreset(json: String): EffectPreset {
        val preset = EffectPresetLoader.load(json)
        register(preset)
        return preset
    }

    /** Looks up any registered package by id. */
    fun findById(id: String): EffectPackage? = packagesById[id]

    /** Looks up a registered transition by id. */
    fun findTransition(id: String): TransitionPackage? = packagesById[id] as? TransitionPackage

    /** Looks up a registered filter by id. */
    fun findFilter(id: String): FilterPackage? = packagesById[id] as? FilterPackage

    /** Looks up a registered effect preset by id. */
    fun findPreset(id: String): EffectPreset? = packagesById[id] as? EffectPreset

    /** Returns every package in a category. Only transition packages declare categories. */
    fun findByCategory(category: String): List<EffectPackage> {
        if (category.isBlank()) return emptyList()
        return packagesById.values.filter { it.category == category }
    }

    /** Returns transition packages in a category. */
    fun findTransitionsByCategory(category: String): List<TransitionPackage> {
        if (category.isBlank()) return emptyList()
        return transitions().filter { it.category == category }
    }

    /** Registered transition packages in insertion order. */
    fun transitions(): List<TransitionPackage> = packagesById.values.filterIsInstance<TransitionPackage>()

    /** Registered filter packages in insertion order. */
    fun filters(): List<FilterPackage> = packagesById.values.filterIsInstance<FilterPackage>()

    /** Registered effect presets in insertion order. */
    fun presets(): List<EffectPreset> = packagesById.values.filterIsInstance<EffectPreset>()

    private fun metadata(effectPackage: EffectPackage): EffectPackageMetadata = when (effectPackage) {
        is TransitionPackage -> EffectPackageMetadata(
            id = effectPackage.id,
            packageVersion = effectPackage.formatVersion,
            name = effectPackage.name,
            category = effectPackage.category,
            parameterDefinitions = effectPackage.parameters,
            license = effectPackage.shader.license,
            assets = listOfNotNull(effectPackage.shader, effectPackage.sound),
        )
        is FilterPackage -> EffectPackageMetadata(
            id = effectPackage.id,
            packageVersion = effectPackage.formatVersion,
            name = effectPackage.name,
            category = effectPackage.category,
            parameterDefinitions = effectPackage.parameters,
            license = effectPackage.license ?: effectPackage.lut.license,
            assets = listOf(effectPackage.lut),
        )
        is EffectPreset -> EffectPackageMetadata(
            id = effectPackage.id,
            packageVersion = effectPackage.formatVersion,
            name = effectPackage.name,
            category = effectPackage.category,
            parameterDefinitions = emptyList(),
            license = null,
            assets = emptyList(),
        )
    }
}
