package org.samvid.orchit.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransitionPackageLoaderTest {
    private val sample = SampleDocuments.read("transition.json")

    @Test
    fun sampleLoadsAndValidates() {
        val package = TransitionPackageLoader.load(sample)

        assertEquals(1, package.formatVersion)
        assertEquals("org.samvid.orchit.transition.dissolve", package.id)
        assertEquals("Dissolve", package.name)
        assertEquals("blend", package.category)
        assertEquals("transition.frag", package.shader.path)
        assertEquals("Apache-2.0", package.shader.license)
        assertEquals(100_000L, package.duration.minUs)
        assertEquals(2_000_000L, package.duration.maxUs)
        assertEquals(1, package.parameters.size)
        assertEquals("progress", package.parameters[0].id)
        assertEquals("float", package.parameters[0].type)
        assertEquals(EffectValue.Decimal(0.0), package.parameters[0].defaultValue)
        assertEquals(0.0, package.parameters[0].minValue ?: Double.NaN, 0.0)
        assertEquals(1.0, package.parameters[0].maxValue ?: Double.NaN, 0.0)
        assertEquals("assets/whoosh.wav", package.sound?.path)
        assertEquals("CC0-1.0", package.sound?.license)
    }

    @Test
    fun optionalSoundMayBeOmitted() {
        val withoutSound = sample.substringBefore(",\n  \"sound\"") + "\n}"

        val package = TransitionPackageLoader.load(withoutSound)

        assertNull(package.sound)
        assertEquals("Dissolve", package.name)
    }

    @Test
    fun rejectsMissingRequiredField() {
        val missingCategory = sample.replace("\"category\": \"blend\",", "")

        assertFormatError("missing required field 'category'") {
            TransitionPackageLoader.load(missingCategory)
        }
    }

    @Test
    fun rejectsUnsupportedFormatVersion() {
        val wrongVersion = sample.replace("\"formatVersion\": 1", "\"formatVersion\": 2")

        assertFormatError("Unsupported transition manifest formatVersion 2") {
            TransitionPackageLoader.load(wrongVersion)
        }
    }

    @Test
    fun rejectsMissingAssetLicense() {
        val missingLicense = sample.replace(",\n    \"license\": \"Apache-2.0\"", "")

        assertFormatError("missing required field 'license'") {
            TransitionPackageLoader.load(missingLicense)
        }
    }

    @Test
    fun rejectsBlankAssetLicense() {
        val blankLicense = sample.replace("\"license\": \"Apache-2.0\"", "\"license\": \"  \"")

        assertFormatError("shader asset license must not be blank") {
            TransitionPackageLoader.load(blankLicense)
        }
    }

    @Test
    fun rejectsMissingSoundLicense() {
        val missingLicense = sample.replace(",\n    \"license\": \"CC0-1.0\"", "")

        assertFormatError("sound asset is missing required field 'license'") {
            TransitionPackageLoader.load(missingLicense)
        }
    }

    @Test
    fun rejectsUnknownFields() {
        val unknown = sample.replace(
            "\"formatVersion\": 1,",
            "\"formatVersion\": 1,\n  \"extra\": true,",
        )

        assertFormatError("contains unknown field(s): extra") {
            TransitionPackageLoader.load(unknown)
        }
    }

    @Test
    fun rejectsInvertedDurationRange() {
        val inverted = sample.replace("\"min\": 100000", "\"min\": 3000000")

        assertFormatError("max must be greater than or equal to min") {
            TransitionPackageLoader.load(inverted)
        }
    }

    @Test
    fun rejectsUnknownParameterFieldAndDuplicateIds() {
        val unknownParameterField = sample.replace(
            "\"id\": \"progress\",",
            "\"id\": \"progress\",\n      \"unit\": \"percent\",",
        )
        assertFormatError("parameters[0] contains unknown field(s): unit") {
            TransitionPackageLoader.load(unknownParameterField)
        }

        val duplicate = sample.replace(
            "\"parameters\": [",
            "\"parameters\": [\n    {\"id\": \"progress\", \"type\": \"float\", \"default\": 0.0},",
        )
        assertFormatError("duplicate parameter id 'progress'") {
            TransitionPackageLoader.load(duplicate)
        }
    }

    @Test
    fun rejectsNetworkAssetPath() {
        val networkPath = sample.replace("transition.frag", "https://example.invalid/transition.frag")

        assertFormatError("must be a local package path") {
            TransitionPackageLoader.load(networkPath)
        }
    }
}
