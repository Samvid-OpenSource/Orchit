package org.samvid.orchit.effects

/** One RGB entry in a parsed 3D LUT. */
data class LutColor(val red: Float, val green: Float, val blue: Float)

/**
 * An immutable 3D LUT with [size] entries per axis.
 *
 * Entries are stored in `.cube` order: red changes fastest, then green, then blue.
 */
class CubeLut internal constructor(val size: Int, sourceComponents: FloatArray) {
    private val components = sourceComponents.copyOf()

    init {
        require(size in CubeLutParser.MIN_SIZE..CubeLutParser.MAX_SIZE) {
            "LUT size must be between ${CubeLutParser.MIN_SIZE} and ${CubeLutParser.MAX_SIZE}"
        }
        val expected = size * size * size * COMPONENTS_PER_ENTRY
        require(this.components.size == expected) {
            "LUT size $size requires $expected components, found ${this.components.size}"
        }
    }

    /** Number of RGB entries in this LUT. */
    val entryCount: Int get() = size * size * size

    /** A copy of the stored components in `.cube` order. */
    val values: FloatArray get() = components.copyOf()

    /** Returns the entry at the given axis indices. */
    operator fun get(red: Int, green: Int, blue: Int): LutColor = colorAt(red, green, blue)

    /** Returns the entry at the given axis indices. */
    fun colorAt(red: Int, green: Int, blue: Int): LutColor {
        requireIndex(red, "red")
        requireIndex(green, "green")
        requireIndex(blue, "blue")
        val entry = red + green * size + blue * size * size
        val offset = entry * COMPONENTS_PER_ENTRY
        return LutColor(
            red = components[offset],
            green = components[offset + 1],
            blue = components[offset + 2],
        )
    }

    override fun equals(other: Any?): Boolean =
        this === other || (
            other is CubeLut &&
                size == other.size &&
                components.contentEquals(other.components)
            )

    override fun hashCode(): Int = 31 * size + components.contentHashCode()

    override fun toString(): String = "CubeLut(size=$size, entries=$entryCount)"

    private fun requireIndex(index: Int, channel: String) {
        require(index in 0 until size) {
            "LUT $channel index $index is outside 0 until $size"
        }
    }

    private companion object {
        const val COMPONENTS_PER_ENTRY = 3
    }
}

/**
 * Parses the textual 3D LUT convention used by `.cube` files.
 *
 * A document declares `LUT_3D_SIZE` and then exactly `size^3` rows of red, green, and blue values.
 * Comments, blank lines, `TITLE`, `DOMAIN_MIN`, and `DOMAIN_MAX` headers are accepted. Data values
 * must be finite and within 0.0 to 1.0.
 */
object CubeLutParser {
    internal const val MIN_SIZE = 2
    internal const val MAX_SIZE = 256

    private const val COMPONENTS_PER_ENTRY = 3
    private val WHITESPACE = Regex("\\s+")

    /** Parses a `.cube` document and rejects malformed sizes, rows, and component values. */
    fun parse(text: String): CubeLut {
        var size: Int? = null
        var domainMin: FloatArray? = null
        var domainMax: FloatArray? = null
        var sawDomainMin = false
        var sawDomainMax = false
        val components = ArrayList<Float>()

        text.removePrefix("\uFEFF").lineSequence().forEachIndexed { index, rawLine ->
            val lineNumber = index + 1
            val line = stripComment(rawLine).trim()
            if (line.isEmpty()) return@forEachIndexed

            val tokens = line.split(WHITESPACE)
            when (tokens[0]) {
                "TITLE" -> readTitle(line, lineNumber)
                "LUT_3D_SIZE" -> {
                    if (size != null) fail(lineNumber, "LUT_3D_SIZE is declared more than once")
                    if (tokens.size != 2) {
                        fail(lineNumber, "LUT_3D_SIZE must be followed by exactly one integer")
                    }
                    val parsedSize = tokens[1].toIntOrNull()
                        ?: fail(lineNumber, "LUT_3D_SIZE must be an integer, found '${tokens[1]}'")
                    if (parsedSize !in MIN_SIZE..MAX_SIZE) {
                        fail(
                            lineNumber,
                            "LUT_3D_SIZE must be between $MIN_SIZE and $MAX_SIZE, found $parsedSize",
                        )
                    }
                    size = parsedSize
                }
                "LUT_1D_SIZE" -> fail(lineNumber, "only 3D LUTs are supported; found LUT_1D_SIZE")
                "DOMAIN_MIN" -> {
                    if (sawDomainMin) fail(lineNumber, "DOMAIN_MIN is declared more than once")
                    sawDomainMin = true
                    domainMin = readDomain(tokens, lineNumber, "DOMAIN_MIN")
                }
                "DOMAIN_MAX" -> {
                    if (sawDomainMax) fail(lineNumber, "DOMAIN_MAX is declared more than once")
                    sawDomainMax = true
                    domainMax = readDomain(tokens, lineNumber, "DOMAIN_MAX")
                }
                else -> {
                    if (
                        tokens.size != COMPONENTS_PER_ENTRY &&
                        tokens[0].firstOrNull()?.isLetter() == true
                    ) {
                        fail(lineNumber, "unknown .cube directive '${tokens[0]}'")
                    }
                    val currentSize = size
                        ?: fail(lineNumber, "LUT data appears before LUT_3D_SIZE")
                    if (tokens.size != COMPONENTS_PER_ENTRY) {
                        fail(lineNumber, "LUT data row must contain exactly red, green, and blue values")
                    }
                    val expectedEntries = currentSize.toLong() * currentSize * currentSize
                    if ((components.size / COMPONENTS_PER_ENTRY).toLong() >= expectedEntries) {
                        fail(
                            lineNumber,
                            "LUT contains more than the $expectedEntries entries required by LUT_3D_SIZE $currentSize",
                        )
                    }
                    tokens.forEach { token ->
                        components += readComponent(token, lineNumber, "LUT component", 0.0f, 1.0f)
                    }
                }
            }
        }

        val finalSize = size ?: throw EffectFormatException(
            "Invalid .cube LUT: missing LUT_3D_SIZE declaration",
        )
        val minimumDomain = domainMin
        val maximumDomain = domainMax
        if (minimumDomain != null && maximumDomain != null) {
            repeat(COMPONENTS_PER_ENTRY) { channel ->
                if (minimumDomain[channel] >= maximumDomain[channel]) {
                    throw EffectFormatException(
                        "Invalid .cube LUT: DOMAIN_MIN must be less than DOMAIN_MAX for every channel",
                    )
                }
            }
        }
        val expectedEntries = finalSize.toLong() * finalSize * finalSize
        val actualEntries = (components.size / COMPONENTS_PER_ENTRY).toLong()
        if (actualEntries != expectedEntries) {
            throw EffectFormatException(
                "Invalid .cube LUT: LUT_3D_SIZE $finalSize requires $expectedEntries data rows, found $actualEntries",
            )
        }
        return CubeLut(finalSize, components.toFloatArray())
    }

    private fun readTitle(line: String, lineNumber: Int) {
        val title = line.substring("TITLE".length).trim()
        if (title.length < 2 || !title.startsWith('"') || !title.endsWith('"')) {
            fail(lineNumber, "TITLE must be a quoted string")
        }
    }

    private fun readDomain(tokens: List<String>, lineNumber: Int, name: String): FloatArray {
        if (tokens.size != COMPONENTS_PER_ENTRY + 1) {
            fail(lineNumber, "$name must be followed by exactly three numbers")
        }
        return FloatArray(COMPONENTS_PER_ENTRY) { channel ->
            readComponent(
                tokens[channel + 1],
                lineNumber,
                name,
                Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY,
            )
        }
    }

    private fun readComponent(
        token: String,
        lineNumber: Int,
        name: String,
        minimum: Float,
        maximum: Float,
    ): Float {
        val component = token.toFloatOrNull()
            ?: fail(lineNumber, "$name '$token' is not a number")
        if (!component.isFinite()) fail(lineNumber, "$name '$token' is not finite")
        if (component < minimum || component > maximum) {
            fail(lineNumber, "$name $token is outside the supported range 0.0 to 1.0")
        }
        return component
    }

    private fun stripComment(line: String): String {
        var inQuotes = false
        var escaped = false
        for (index in line.indices) {
            val current = line[index]
            if (escaped) {
                escaped = false
                continue
            }
            when (current) {
                '\\' -> if (inQuotes) escaped = true
                '"' -> inQuotes = !inQuotes
                '#' -> if (!inQuotes) return line.substring(0, index)
            }
        }
        return line
    }

    private fun fail(lineNumber: Int, message: String): Nothing =
        throw EffectFormatException("Invalid .cube LUT at line $lineNumber: $message")
}
