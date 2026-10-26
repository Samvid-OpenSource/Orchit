package org.samvid.orchit.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FilterPackageLoaderTest {
    private val sample = SampleDocuments.read("filter.json")

    @Test
    fun sampleLoadsAndValidates() {
        val package = FilterPackageLoader.load(sample)

        assertEquals(1, package.formatVersion)
        assertEquals("org.samvid.orchit.filter.warm", package.id)
        assertEquals("Warm", package.name)
        assertEquals("warm.cube", package.lut.path)
        assertEquals("CC0-1.0", package.lut.license)
        assertNull(package.category)
        assertNull(package.license)
        assertEquals(1, package.parameters.size)
        assertEquals("intensity", package.parameters[0].id)
        assertEquals(EffectValue.Decimal(1.0), package.parameters[0].defaultValue)
    }

    @Test
    fun acceptsDeclaredPackageLicense() {
        val withPackageLicense = sample.replace(
            "\"name\": \"Warm\",",
            "\"name\": \"Warm\",\n  \"license\": \"CC0-1.0\",",
        )

        val package = FilterPackageLoader.load(withPackageLicense)

        assertEquals("CC0-1.0", package.license)
    }

    @Test
    fun rejectsMissingRequiredField() {
        val missingParameters = sample.replace(
            """
            ,
              "parameters": [
                {
                  "id": "intensity",
                  "type": "float",
                  "default": 1.0,
                  "min": 0.0,
                  "max": 1.0
                }
              ]
            """.trimIndent(),
            "",
        )

        assertFormatError("missing required field 'parameters'") {
            FilterPackageLoader.load(missingParameters)
        }
    }

    @Test
    fun rejectsUnsupportedFormatVersion() {
        val wrongVersion = sample.replace("\"formatVersion\": 1", "\"formatVersion\": 2")

        assertFormatError("Unsupported filter manifest formatVersion 2") {
            FilterPackageLoader.load(wrongVersion)
        }
    }

    @Test
    fun rejectsMissingLutLicense() {
        val missingLicense = sample.replace(",\n    \"license\": \"CC0-1.0\"", "")

        assertFormatError("missing required field 'license'") {
            FilterPackageLoader.load(missingLicense)
        }
    }

    @Test
    fun rejectsBlankPackageLicense() {
        val blankLicense = sample.replace(
            "\"name\": \"Warm\",",
            "\"name\": \"Warm\",\n  \"license\": \" \",",
        )

        assertFormatError("filter manifest license must not be blank") {
            FilterPackageLoader.load(blankLicense)
        }
    }

    @Test
    fun rejectsUnknownFields() {
        val unknown = sample.replace(
            "\"formatVersion\": 1,",
            "\"formatVersion\": 1,\n  \"category\": \"color\",",
        )

        assertFormatError("contains unknown field(s): category") {
            FilterPackageLoader.load(unknown)
        }
    }

    @Test
    fun rejectsNonCubeLutReference() {
        val wrongAsset = sample.replace("warm.cube", "warm.png")

        assertFormatError("must identify a .cube file") {
            FilterPackageLoader.load(wrongAsset)
        }
    }
}
