package org.samvid.orchit.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectPresetLoaderTest {
    private val sample = SampleDocuments.read("effect-preset.json")

    @Test
    fun sampleLoadsAndValidates() {
        val preset = EffectPresetLoader.load(sample)

        assertEquals(1, preset.formatVersion)
        assertEquals("org.samvid.orchit.effect.exposure", preset.id)
        assertEquals("Soft exposure", preset.name)
        assertEquals("org.samvid.orchit.color.adjust", preset.effectId)
        assertEquals(EffectValue.Decimal(0.15), preset.parameters["exposure"])
        assertEquals(EffectValue.Decimal(1.05), preset.parameters["contrast"])
        assertEquals(1, preset.keyframes.size)
        assertEquals(0L, preset.keyframes[0].timeUs)
        assertEquals(EffectValue.Decimal(0.15), preset.keyframes[0].values["exposure"])
    }

    @Test
    fun optionalKeyframesMayBeOmitted() {
        val withoutKeyframes = sample.substringBefore(",\n  \"keyframes\"") + "\n}"

        val preset = EffectPresetLoader.load(withoutKeyframes)

        assertTrue(preset.keyframes.isEmpty())
    }

    @Test
    fun rejectsMissingRequiredField() {
        val missingEffectId = sample.replace("\"effectId\": \"org.samvid.orchit.color.adjust\",", "")

        assertFormatError("missing required field 'effectId'") {
            EffectPresetLoader.load(missingEffectId)
        }
    }

    @Test
    fun rejectsUnsupportedFormatVersion() {
        val wrongVersion = sample.replace("\"formatVersion\": 1", "\"formatVersion\": 2")

        assertFormatError("Unsupported effect preset formatVersion 2") {
            EffectPresetLoader.load(wrongVersion)
        }
    }

    @Test
    fun rejectsUnknownFields() {
        val unknown = sample.replace(
            "\"formatVersion\": 1,",
            "\"formatVersion\": 1,\n  \"category\": \"color\",",
        )

        assertFormatError("contains unknown field(s): category") {
            EffectPresetLoader.load(unknown)
        }
    }

    @Test
    fun rejectsObjectParameterValues() {
        val objectValue = sample.replace(
            "\"exposure\": 0.15,",
            "\"exposure\": {\"amount\": 0.15},",
        )

        assertFormatError("parameters.exposure must be a JSON scalar or array") {
            EffectPresetLoader.load(objectValue)
        }
    }

    @Test
    fun rejectsNullParameterValues() {
        val nullValue = sample.replace("\"contrast\": 1.05", "\"contrast\": null")

        assertFormatError("parameters.contrast must be a JSON scalar or array") {
            EffectPresetLoader.load(nullValue)
        }
    }

    @Test
    fun rejectsNegativeKeyframeTime() {
        val negativeTime = sample.replace("\"timeUs\": 0", "\"timeUs\": -1")

        assertFormatError("keyframes[0] timeUs must not be negative") {
            EffectPresetLoader.load(negativeTime)
        }
    }

    @Test
    fun rejectsUnknownKeyframeField() {
        val unknown = sample.replace(
            "\"timeUs\": 0,",
            "\"timeUs\": 0,\n      \"easing\": \"linear\",",
        )

        assertFormatError("keyframes[0] contains unknown field(s): easing") {
            EffectPresetLoader.load(unknown)
        }
    }
}
