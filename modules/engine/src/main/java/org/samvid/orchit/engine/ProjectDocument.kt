package org.samvid.orchit.engine

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/** Version 1 of the portable local project document format. */
data class ProjectDocument(
    override val formatVersion: Int,
    override val projectId: String,
    override val name: String,
    override val durationUs: Long,
    override val tracks: List<ProjectTrackDocument>,
) : ProjectDocumentContract

/** A version 1 project track. */
data class ProjectTrackDocument(
    val id: String,
    val kind: String,
    val clips: List<ProjectClipDocument>,
)

/** A clip document with its local asset, timing, and optional visual effects. */
data class ProjectClipDocument(
    val id: String,
    val asset: ProjectAssetDocument,
    val startUs: Long,
    val sourceInUs: Long,
    val durationUs: Long,
    val effects: List<ProjectEffectDocument> = emptyList(),
)

/** A bundled or locally stored asset with an SPDX license identifier. */
data class ProjectAssetDocument(val path: String, val license: String)

/** An effect entry attached to a project clip. */
data class ProjectEffectDocument(
    val effectId: String,
    val parameters: JsonObject,
    val keyframes: List<ProjectKeyframeDocument> = emptyList(),
)

/** A clip-relative effect keyframe and its named property values. */
data class ProjectKeyframeDocument(val timeUs: Long, val values: JsonObject)

/** Reads and writes strict version 1 project JSON documents. */
object ProjectJson {
    private val gson = GsonBuilder().disableHtmlEscaping().create()

    /** Parses a project document, rejecting unsupported versions and unknown fields. */
    fun read(json: String): ProjectDocument {
        val root = JsonParser.parseString(json)
        require(root.isJsonObject) { "Project must be a JSON object" }
        val obj = root.asJsonObject
        obj.requireFields("formatVersion", "projectId", "name", "durationUs", "tracks")
        obj.rejectUnknown("formatVersion", "projectId", "name", "durationUs", "tracks")
        require(obj.get("formatVersion").asInt == CURRENT_FORMAT_VERSION) { "Unsupported project format version" }
        val tracks = obj.get("tracks")
        require(tracks.isJsonArray) { "tracks must be an array" }
        val document = ProjectDocument(
            formatVersion = obj.get("formatVersion").asInt,
            projectId = obj.get("projectId").asString,
            name = obj.get("name").asString,
            durationUs = obj.get("durationUs").asLong,
            tracks = tracks.asJsonArray.map { trackElement ->
                val track = trackElement.asJsonObject
                track.requireFields("id", "kind", "clips")
                track.rejectUnknown("id", "kind", "clips")
                require(track.get("clips").isJsonArray) { "clips must be an array" }
                ProjectTrackDocument(
                    track.get("id").asString,
                    track.get("kind").asString,
                    track.get("clips").asJsonArray.map { clipElement ->
                        val clip = clipElement.asJsonObject
                        clip.requireFields("id", "asset", "startUs", "sourceInUs", "durationUs")
                        clip.rejectUnknown("id", "asset", "startUs", "sourceInUs", "durationUs", "effects")
                        val asset = clip.get("asset").asJsonObject
                        asset.requireFields("path", "license")
                        asset.rejectUnknown("path", "license")
                        val effects = clip.get("effects")?.asJsonArray ?: com.google.gson.JsonArray()
                        ProjectClipDocument(
                            id = clip.get("id").asString,
                            asset = ProjectAssetDocument(asset.get("path").asString, asset.get("license").asString),
                            startUs = clip.get("startUs").asLong,
                            sourceInUs = clip.get("sourceInUs").asLong,
                            durationUs = clip.get("durationUs").asLong,
                            effects = effects.map { effectElement ->
                                val effect = effectElement.asJsonObject
                                effect.requireFields("effectId", "parameters")
                                effect.rejectUnknown("effectId", "parameters", "keyframes")
                                val keyframes = effect.get("keyframes")?.asJsonArray ?: com.google.gson.JsonArray()
                                ProjectEffectDocument(
                                    effectId = effect.get("effectId").asString,
                                    parameters = effect.get("parameters").asJsonObject,
                                    keyframes = keyframes.map { keyframeElement ->
                                        val keyframe = keyframeElement.asJsonObject
                                        keyframe.requireFields("timeUs", "values")
                                        keyframe.rejectUnknown("timeUs", "values")
                                        ProjectKeyframeDocument(keyframe.get("timeUs").asLong, keyframe.get("values").asJsonObject)
                                    },
                                )
                            },
                        )
                    },
                )
            },
        )
        document.validate()
        return document
    }

    /** Serializes a validated project document as UTF-8-ready JSON text. */
    fun write(document: ProjectDocument): String {
        document.validate()
        return gson.toJson(document)
    }

    private fun ProjectDocument.validate() {
        require(formatVersion == CURRENT_FORMAT_VERSION) { "Unsupported project format version" }
        require(projectId.isNotBlank())
        require(name.isNotBlank())
        require(durationUs >= 0)
        tracks.forEach { track ->
            require(track.kind in setOf("video", "audio", "text"))
            track.clips.forEach { clip ->
                require(clip.startUs >= 0 && clip.sourceInUs >= 0 && clip.durationUs > 0)
                require(clip.asset.path.isNotBlank() && clip.asset.license.isNotBlank())
                clip.effects.forEach { effect ->
                    require(effect.effectId.isNotBlank())
                    effect.keyframes.forEach { require(it.timeUs >= 0) }
                }
            }
        }
    }

    private fun JsonObject.requireFields(vararg names: String) {
        names.forEach { require(has(it)) { "Missing required field: $it" } }
    }

    private fun JsonObject.rejectUnknown(vararg names: String) {
        val allowed = names.toSet()
        val unknown = keySet() - allowed
        require(unknown.isEmpty()) { "Unknown fields: ${unknown.joinToString()}" }
    }

    private const val CURRENT_FORMAT_VERSION = 1
}
