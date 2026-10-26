package org.samvid.orchit.effects

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledPackagesTest {
    @Test
    fun loadsEveryBundledTransition() {
        val directories = packageDirectories("transitions")
        assertEquals(10, directories.size)

        val registry = EffectPackageRegistry()
        val ids = mutableSetOf<String>()
        directories.forEach { directory ->
            val manifest = File(directory, "manifest.json")
            assertTrue("Missing transition manifest ${manifest.path}", manifest.isFile)

            val transition = registry.loadTransition(manifest.readText())
            assertTrue("Duplicate transition id ${transition.id}", ids.add(transition.id))
            assertEquals(
                "org.samvid.orchit.transition.${directory.name}",
                transition.id,
            )
            assertEquals("blend", transition.category)
            assertEquals("Apache-2.0", transition.shader.license)
            assertEquals(listOf("progress"), transition.parameters.map { it.id })

            val shader = File(directory, transition.shader.path)
            assertTrue("Missing shader ${shader.path}", shader.isFile)
            assertTrue("Shader ${shader.path} must not be empty", shader.readText().isNotBlank())
        }

        assertEquals(10, registry.transitions().size)
        assertEquals(10, ids.size)
    }

    @Test
    fun loadsEveryBundledFilter() {
        val directories = packageDirectories("filters")
        assertEquals(3, directories.size)

        val registry = EffectPackageRegistry()
        val ids = mutableSetOf<String>()
        directories.forEach { directory ->
            val manifest = File(directory, "manifest.json")
            assertTrue("Missing filter manifest ${manifest.path}", manifest.isFile)

            val filter = registry.loadFilter(manifest.readText())
            assertTrue("Duplicate filter id ${filter.id}", ids.add(filter.id))
            assertEquals("org.samvid.orchit.filter.${directory.name}", filter.id)
            assertEquals("Apache-2.0", filter.lut.license)
            assertEquals(listOf("intensity"), filter.parameters.map { it.id })

            val lutFile = File(directory, filter.lut.path)
            assertTrue("Missing LUT ${lutFile.path}", lutFile.isFile)
            assertTrue("LUT ${lutFile.path} must not be empty", lutFile.readText().isNotBlank())
            assertEquals(17, CubeLutParser.parse(lutFile.readText()).size)
        }

        assertEquals(3, registry.filters().size)
        assertEquals(3, ids.size)
    }

    @Test
    fun registersEveryBundledPackageTogether() {
        val registry = EffectPackageRegistry()
        val ids = mutableSetOf<String>()

        packageDirectories("transitions").forEach { directory ->
            val transition = registry.loadTransition(File(directory, "manifest.json").readText())
            assertTrue("Duplicate bundle id ${transition.id}", ids.add(transition.id))
            assertTrue(registry.findTransition(transition.id) === transition)
        }
        packageDirectories("filters").forEach { directory ->
            val filter = registry.loadFilter(File(directory, "manifest.json").readText())
            assertTrue("Duplicate bundle id ${filter.id}", ids.add(filter.id))
            assertTrue(registry.findFilter(filter.id) === filter)
        }

        assertEquals(13, registry.size)
        assertEquals(13, ids.size)
        assertEquals(10, registry.transitions().size)
        assertEquals(3, registry.filters().size)
    }

    private fun packageDirectories(kind: String): List<File> {
        val directories = File(assetsRoot, kind).listFiles { file -> file.isDirectory }
            ?: throw AssertionError("Missing bundled assets directory for $kind")
        return directories.sortedBy { it.name }
    }

    private companion object {
        private val assetsRoot: File = locateAssetsRoot()

        private fun locateAssetsRoot(): File {
            var directory: File? = File("").absoluteFile
            while (directory != null) {
                val candidates = listOf(
                    File(directory, "src/main/assets"),
                    File(directory, "modules/effects/src/main/assets"),
                )
                candidates.firstOrNull { it.isDirectory }?.let { return it }
                directory = directory.parentFile
            }
            throw AssertionError("Could not locate modules/effects/src/main/assets")
        }
    }
}
