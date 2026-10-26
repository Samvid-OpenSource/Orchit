package org.samvid.orchit.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CubeLutParserTest {
    private val unitRows = listOf(
        "0.0 0.0 0.0",
        "1.0 0.0 0.0",
        "0.0 1.0 0.0",
        "1.0 1.0 0.0",
        "0.0 0.0 1.0",
        "1.0 0.0 1.0",
        "0.0 1.0 1.0",
        "1.0 1.0 1.0",
    )

    @Test
    fun sampleParsesWithExpectedSizeAndOrdering() {
        val lut = CubeLutParser.parse(SampleDocuments.read("warm.cube"))

        assertEquals(2, lut.size)
        assertEquals(8, lut.entryCount)
        assertEquals(24, lut.values.size)
        assertEquals(LutColor(0f, 0f, 0f), lut[0, 0, 0])
        assertEquals(LutColor(1f, 0f, 0f), lut[1, 0, 0])
        assertEquals(LutColor(0f, 1f, 0f), lut[0, 1, 0])
        assertEquals(LutColor(0f, 0f, 1f), lut[0, 0, 1])
        assertEquals(LutColor(1f, 1f, 1f), lut[1, 1, 1])
    }

    @Test
    fun acceptsCommentsTitleBlankLinesAndDomains() {
        val source = buildString {
            appendLine("TITLE \"Orchit sample # with a hash\"")
            appendLine("# leading comment")
            appendLine("DOMAIN_MIN 0.0 0.0 0.0")
            appendLine("DOMAIN_MAX 1.0 1.0 1.0")
            appendLine("LUT_3D_SIZE 2")
            appendLine()
            unitRows.forEach { appendLine("$it # trailing comment") }
        }

        val lut = CubeLutParser.parse(source)

        assertEquals(2, lut.size)
        assertEquals(LutColor(1f, 0f, 0f), lut[1, 0, 0])
    }

    @Test
    fun rejectsWrongEntryCount() {
        val source = "LUT_3D_SIZE 2\n" + unitRows.take(7).joinToString("\n")

        assertFormatError("LUT_3D_SIZE 2 requires 8 data rows, found 7") {
            CubeLutParser.parse(source)
        }
    }

    @Test
    fun rejectsTooManyEntries() {
        val source = "LUT_3D_SIZE 2\n" + (unitRows + unitRows.first()).joinToString("\n")

        assertFormatError("more than the 8 entries required by LUT_3D_SIZE 2") {
            CubeLutParser.parse(source)
        }
    }

    @Test
    fun rejectsOutOfRangeComponents() {
        val tooHigh = "LUT_3D_SIZE 2\n" +
            (listOf("1.5 0.0 0.0") + unitRows.drop(1)).joinToString("\n")
        assertFormatError("LUT component 1.5 is outside the supported range 0.0 to 1.0") {
            CubeLutParser.parse(tooHigh)
        }

        val negative = "LUT_3D_SIZE 2\n" +
            (listOf("-0.1 0.0 0.0") + unitRows.drop(1)).joinToString("\n")
        assertFormatError("LUT component -0.1 is outside the supported range 0.0 to 1.0") {
            CubeLutParser.parse(negative)
        }
    }

    @Test
    fun rejectsInvalidSizeDeclarations() {
        assertFormatError("LUT_3D_SIZE must be between 2 and 256, found 1") {
            CubeLutParser.parse("LUT_3D_SIZE 1\n0.0 0.0 0.0")
        }
        assertFormatError("LUT_3D_SIZE must be an integer") {
            CubeLutParser.parse("LUT_3D_SIZE 2.0")
        }
        assertFormatError("LUT_3D_SIZE is declared more than once") {
            CubeLutParser.parse("LUT_3D_SIZE 2\nLUT_3D_SIZE 2")
        }
        assertFormatError("only 3D LUTs are supported") {
            CubeLutParser.parse("LUT_1D_SIZE 2")
        }
    }

    @Test
    fun rejectsMissingSizeAndDataBeforeSize() {
        assertFormatError("missing LUT_3D_SIZE declaration") {
            CubeLutParser.parse("# an empty LUT\n")
        }
        assertFormatError("LUT data appears before LUT_3D_SIZE") {
            CubeLutParser.parse("0.0 0.0 0.0")
        }
    }

    @Test
    fun rejectsMalformedRowsAndComponents() {
        assertFormatError("LUT data row must contain exactly red, green, and blue values") {
            CubeLutParser.parse("LUT_3D_SIZE 2\n0.0 0.0")
        }
        assertFormatError("LUT component 'red' is not a number") {
            CubeLutParser.parse("LUT_3D_SIZE 2\nred 0.0 0.0")
        }
        assertFormatError("LUT component 'NaN' is not finite") {
            CubeLutParser.parse("LUT_3D_SIZE 2\nNaN 0.0 0.0")
        }
    }

    @Test
    fun rejectsUnknownDirective() {
        assertFormatError("unknown .cube directive 'LUT_PREVIEW'") {
            CubeLutParser.parse("LUT_PREVIEW 1\nLUT_3D_SIZE 2")
        }
    }

    @Test
    fun rejectsInvalidDomain() {
        val source = "LUT_3D_SIZE 2\nDOMAIN_MIN 1 1 1\nDOMAIN_MAX 0 0 0\n" +
            unitRows.joinToString("\n")

        assertFormatError("DOMAIN_MIN must be less than DOMAIN_MAX") {
            CubeLutParser.parse(source)
        }
    }

    @Test
    fun rejectsIndexesOutsideTheLut() {
        val lut = CubeLutParser.parse("LUT_3D_SIZE 2\n" + unitRows.joinToString("\n"))

        val error = assertThrows(IllegalArgumentException::class.java) {
            lut.colorAt(2, 0, 0)
        }
        assertTrue(error.message.orEmpty().contains("LUT red index 2 is outside 0 until 2"))
        assertTrue(lut.values.all { it in 0f..1f })
    }
}
