package org.samvid.orchit.effects

import java.math.BigDecimal

/**
 * Minimal strict JSON tree used by the effects format readers.
 *
 * The reader is deliberately self-contained so package validation stays local, offline, and usable
 * in plain JVM tests without Android APIs.
 */
internal sealed interface JsonValue {
    data class ObjectValue(val fields: LinkedHashMap<String, JsonValue>) : JsonValue
    data class ArrayValue(val values: List<JsonValue>) : JsonValue
    data class StringValue(val value: String) : JsonValue
    data class NumberValue(val value: BigDecimal, val integerLiteral: Boolean) : JsonValue
    data class BooleanValue(val value: Boolean) : JsonValue
    data object NullValue : JsonValue
}

/** Parses the strict JSON subset used by Orchit format documents. */
internal object JsonParser {
    fun parse(text: String, description: String): JsonValue {
        val source = text.removePrefix("\uFEFF")
        return Reader(source, description).parseDocument()
    }

    private class Reader(
        private val text: String,
        private val description: String,
    ) {
        private var index = 0

        fun parseDocument(): JsonValue {
            skipWhitespace()
            if (index >= text.length) fail("expected a value")
            val value = parseValue()
            skipWhitespace()
            if (index < text.length) fail("unexpected trailing content")
            return value
        }

        private fun parseValue(): JsonValue = when (val current = peek()) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> JsonValue.StringValue(parseString())
            't' -> {
                expectLiteral("true")
                JsonValue.BooleanValue(true)
            }
            'f' -> {
                expectLiteral("false")
                JsonValue.BooleanValue(false)
            }
            'n' -> {
                expectLiteral("null")
                JsonValue.NullValue
            }
            '-' -> parseNumber()
            in '0'..'9' -> parseNumber()
            else -> fail("unexpected character '$current'")
        }

        private fun parseObject(): JsonValue.ObjectValue {
            index++
            val fields = LinkedHashMap<String, JsonValue>()
            skipWhitespace()
            if (peekOrNull() == '}') {
                index++
                return JsonValue.ObjectValue(fields)
            }
            while (true) {
                skipWhitespace()
                if (peekOrNull() != '"') fail("expected an object field name")
                val name = parseString()
                skipWhitespace()
                if (peekOrNull() != ':') fail("expected ':' after object field '$name'")
                index++
                skipWhitespace()
                val value = parseValue()
                if (fields.put(name, value) != null) fail("duplicate object field '$name'")
                skipWhitespace()
                when (peekOrNull()) {
                    ',' -> index++
                    '}' -> {
                        index++
                        return JsonValue.ObjectValue(fields)
                    }
                    null -> fail("unexpected end of input in object")
                    else -> fail("expected ',' or '}' after object field '$name'")
                }
            }
        }

        private fun parseArray(): JsonValue.ArrayValue {
            index++
            val values = ArrayList<JsonValue>()
            skipWhitespace()
            if (peekOrNull() == ']') {
                index++
                return JsonValue.ArrayValue(values)
            }
            while (true) {
                skipWhitespace()
                values += parseValue()
                skipWhitespace()
                when (peekOrNull()) {
                    ',' -> index++
                    ']' -> {
                        index++
                        return JsonValue.ArrayValue(values)
                    }
                    null -> fail("unexpected end of input in array")
                    else -> fail("expected ',' or ']' after array value")
                }
            }
        }

        private fun parseString(): String {
            index++
            val result = StringBuilder()
            while (true) {
                if (index >= text.length) fail("unterminated string")
                when (val current = text[index++]) {
                    '"' -> return result.toString()
                    '\\' -> result.append(parseEscape())
                    in '\u0000'..'\u001F' -> fail("unescaped control character in string")
                    else -> result.append(current)
                }
            }
        }

        private fun parseEscape(): String {
            if (index >= text.length) fail("incomplete escape sequence")
            return when (val escape = text[index++]) {
                '"' -> "\""
                '\\' -> "\\"
                '/' -> "/"
                'b' -> "\b"
                'f' -> "\u000C"
                'n' -> "\n"
                'r' -> "\r"
                't' -> "\t"
                'u' -> parseUnicodeEscape()
                else -> fail("invalid escape sequence '\\$escape'")
            }
        }

        private fun parseUnicodeEscape(): String {
            if (index + 4 > text.length) fail("incomplete unicode escape")
            var codePoint = 0
            repeat(4) {
                val digit = Character.digit(text[index++], 16)
                if (digit < 0) fail("invalid unicode escape")
                codePoint = codePoint * 16 + digit
            }
            return codePoint.toChar().toString()
        }

        private fun parseNumber(): JsonValue.NumberValue {
            val start = index
            if (text[index] == '-') index++
            if (index >= text.length) fail("incomplete number")
            when (val current = text[index]) {
                '0' -> {
                    index++
                    if (peekOrNull()?.isDigit() == true) fail("leading zero in number")
                }
                in '1'..'9' -> while (peekOrNull()?.isDigit() == true) index++
                else -> fail("expected a digit in number, found '$current'")
            }
            var integerLiteral = true
            if (peekOrNull() == '.') {
                integerLiteral = false
                index++
                if (peekOrNull()?.isDigit() != true) fail("expected a digit after the decimal point")
                while (peekOrNull()?.isDigit() == true) index++
            }
            if (peekOrNull() == 'e' || peekOrNull() == 'E') {
                integerLiteral = false
                index++
                if (peekOrNull() == '+' || peekOrNull() == '-') index++
                if (peekOrNull()?.isDigit() != true) fail("expected a digit in the exponent")
                while (peekOrNull()?.isDigit() == true) index++
            }
            val raw = text.substring(start, index)
            val number = try {
                BigDecimal(raw)
            } catch (exception: NumberFormatException) {
                fail("invalid number '$raw' (${exception.message})")
            }
            return JsonValue.NumberValue(number, integerLiteral)
        }

        private fun expectLiteral(literal: String) {
            if (!text.startsWith(literal, index)) fail("expected '$literal'")
            index += literal.length
        }

        private fun skipWhitespace() {
            while (index < text.length && text[index] in WHITESPACE) index++
        }

        private fun peek(): Char = peekOrNull() ?: fail("unexpected end of input")

        private fun peekOrNull(): Char? = if (index < text.length) text[index] else null

        private fun fail(message: String): Nothing {
            var line = 1
            var column = 1
            for (position in 0 until minOf(index, text.length)) {
                if (text[position] == '\n') {
                    line++
                    column = 1
                } else {
                    column++
                }
            }
            throw EffectFormatException("Invalid $description JSON at line $line, column $column: $message")
        }

        private companion object {
            val WHITESPACE = charArrayOf(' ', '\t', '\n', '\r')
        }
    }
}

/** Returns this value as an object or fails with a format error. */
internal fun JsonValue.asObject(context: String): JsonValue.ObjectValue =
    this as? JsonValue.ObjectValue
        ?: throw EffectFormatException("$context must be a JSON object")

/** Returns this value as an array or fails with a format error. */
internal fun JsonValue.asArray(context: String): JsonValue.ArrayValue =
    this as? JsonValue.ArrayValue
        ?: throw EffectFormatException("$context must be a JSON array")

/** Returns this value as a string or fails with a format error. */
internal fun JsonValue.asString(context: String): String =
    (this as? JsonValue.StringValue)?.value
        ?: throw EffectFormatException("$context must be a string")

/** Returns this value as a nonblank string or fails with a format error. */
internal fun JsonValue.asNonBlankString(context: String): String {
    val value = asString(context)
    if (value.isBlank()) throw EffectFormatException("$context must not be blank")
    return value
}

/** Returns this value as a local package path or fails with a format error. */
internal fun JsonValue.asLocalPath(context: String): String {
    val value = asNonBlankString(context)
    if (value.contains("://")) {
        throw EffectFormatException("$context must be a local package path, not a network URL")
    }
    return value
}

/** Returns this value as an exact long or fails with a format error. */
internal fun JsonValue.asLong(context: String): Long {
    val number = asNumber(context).value.stripTrailingZeros()
    if (number.scale() > 0) throw EffectFormatException("$context must be an integer")
    return try {
        number.longValueExact()
    } catch (exception: ArithmeticException) {
        throw EffectFormatException("$context is outside the supported integer range")
    }
}

/** Returns this value as a finite double or fails with a format error. */
internal fun JsonValue.asDouble(context: String): Double {
    val value = asNumber(context).value.toDouble()
    if (!value.isFinite()) {
        throw EffectFormatException("$context is outside the supported number range")
    }
    return value
}

private fun JsonValue.asNumber(context: String): JsonValue.NumberValue =
    this as? JsonValue.NumberValue
        ?: throw EffectFormatException("$context must be a number")

/** Reads a required field from this object or fails with a format error. */
internal fun JsonValue.ObjectValue.requiredField(name: String, context: String): JsonValue =
    fields[name] ?: throw EffectFormatException("$context is missing required field '$name'")

/** Ensures every named field exists on this object. */
internal fun JsonValue.ObjectValue.requireFields(names: List<String>, context: String) {
    names.forEach { requiredField(it, context) }
}

/** Rejects object keys outside the format's permitted field set. */
internal fun JsonValue.ObjectValue.rejectUnknownFields(allowed: Set<String>, context: String) {
    val unknown = fields.keys - allowed
    if (unknown.isNotEmpty()) {
        throw EffectFormatException("$context contains unknown field(s): ${unknown.sorted().joinToString(", ")}")
    }
}
