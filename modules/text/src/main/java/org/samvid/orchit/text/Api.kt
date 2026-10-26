package org.samvid.orchit.text

/** Font asset resolved from local bundled or imported storage. */
data class FontAsset(val uri: String, val licenseSpdx: String)
/** Text overlay positioned in timeline and canvas coordinates. */
data class TextOverlay(val id: String, val text: String, val font: FontAsset?, val startUs: Long, val durationUs: Long)
/** Animates text overlay properties over its duration. */
interface TextAnimator { fun applyPreset(overlayId: String, presetId: String) }
/** Catalog for fonts and text animation presets. */
interface TextCatalog { fun presetIds(): List<String>; fun fontAssets(): List<FontAsset> }
