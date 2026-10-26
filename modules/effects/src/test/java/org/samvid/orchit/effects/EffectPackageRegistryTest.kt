package org.samvid.orchit.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectPackageRegistryTest {
    private val transitionJson = SampleDocuments.read("transition.json")
    private val filterJson = SampleDocuments.read("filter.json")
    private val presetJson = SampleDocuments.read("effect-preset.json")

    @Test
    fun loadsPackagesAndLooksThemUpById() {
        val registry = EffectPackageRegistry()

        val transition = registry.loadTransition(transitionJson)
        val filter = registry.loadFilter(filterJson)
        val preset = registry.loadPreset(presetJson)

        assertEquals(3, registry.size)
        assertSame(transition, registry.findById(transition.id))
        assertSame(filter, registry.findById(filter.id))
        assertSame(preset, registry.findById(preset.id))
        assertSame(transition, registry.findTransition(transition.id))
        assertSame(filter, registry.findFilter(filter.id))
        assertSame(preset, registry.findPreset(preset.id))
        assertNull(registry.findById("org.samvid.orchit.missing"))
        assertEquals(listOf(transition), registry.transitions())
        assertEquals(listOf(filter), registry.filters())
        assertEquals(listOf(preset), registry.presets())
    }

    @Test
    fun looksPackagesUpByCategory() {
        val registry = EffectPackageRegistry()
        val transition = registry.loadTransition(transitionJson)
        registry.loadFilter(filterJson)
        registry.loadPreset(presetJson)

        assertEquals(listOf(transition), registry.findByCategory("blend"))
        assertEquals(listOf(transition), registry.findTransitionsByCategory("blend"))
        assertTrue(registry.findByCategory("color").isEmpty())
        assertTrue(registry.findTransitionsByCategory("color").isEmpty())
        assertTrue(registry.findByCategory(" ").isEmpty())
    }

    @Test
    fun rejectsDuplicatePackageIds() {
        val registry = EffectPackageRegistry()
        registry.loadTransition(transitionJson)

        assertFormatError("Duplicate effect package id 'org.samvid.orchit.transition.dissolve'") {
            registry.loadTransition(transitionJson)
        }
    }

    @Test
    fun keepsRegistrationOrder() {
        val registry = EffectPackageRegistry()
        val transition = registry.loadTransition(transitionJson)
        val filter = registry.loadFilter(filterJson)
        val preset = registry.loadPreset(presetJson)

        assertEquals(listOf<EffectPackage>(transition, filter, preset), registry.packages())
    }
}
