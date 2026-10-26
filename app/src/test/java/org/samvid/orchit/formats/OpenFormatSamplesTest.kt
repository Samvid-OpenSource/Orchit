package org.samvid.orchit.formats

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenFormatSamplesTest {
    private val formats = listOf(
        "project.json",
        "transition.json",
        "filter.json",
        "text-animation.json",
        "effect-preset.json",
    )

    @Test
    fun samplesParseAndDeclareSupportedVersion() {
        formats.forEach { name ->
            val sample = resource(name)
            assertTrue("$name must be a JSON object", sample.isJsonObject)
            assertEquals("$name format version", 1, sample.asJsonObject.get("formatVersion").asInt)
        }
    }

    @Test
    fun samplesContainFormatRequiredStructure() {
        assertTrue(resource("project.json").asJsonObject.getAsJsonArray("tracks").size() > 0)
        assertNotNull(resource("transition.json").asJsonObject.get("shader"))
        assertTrue(resource("filter.json").asJsonObject.getAsJsonObject("lut").get("path").asString.endsWith(".cube"))
        assertTrue(resource("text-animation.json").asJsonObject.getAsJsonArray("keyframes").size() >= 2)
        assertNotNull(resource("effect-preset.json").asJsonObject.get("effectId"))
    }

    @Test
    fun declaredBundledAssetsHaveSpdxLicenseIdentifiers() {
        formats.map(::resource).forEach { root ->
            val assets = mutableListOf<JsonObject>()
            collectAssetObjects(root, assets)
            assets.forEach { asset ->
                assertTrue("asset must include a nonempty license", asset.has("license") && asset["license"].asString.isNotBlank())
            }
        }
    }

    private fun resource(name: String): JsonElement =
        javaClass.classLoader!!.getResourceAsStream("formats/$name")!!.bufferedReader().use {
            JsonParser.parseReader(it)
        }

    private fun collectAssetObjects(node: JsonElement, output: MutableList<JsonObject>) {
        if (node.isJsonObject) {
            val obj = node.asJsonObject
            if (obj.has("path") && obj.has("license")) output += obj
            obj.entrySet().forEach { collectAssetObjects(it.value, output) }
        } else if (node.isJsonArray) {
            node.asJsonArray.forEach { collectAssetObjects(it, output) }
        }
    }
}
