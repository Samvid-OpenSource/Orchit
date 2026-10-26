package org.samvid.orchit.effects

/** Reads format samples from the test classpath. */
internal object SampleDocuments {
    fun read(name: String): String {
        val stream = SampleDocuments::class.java.classLoader!!.getResourceAsStream(name)
            ?: throw AssertionError("Missing test resource '$name'")
        return stream.bufferedReader().use { it.readText() }
    }
}

/** Asserts that a format loader rejects a document with a useful message. */
internal inline fun assertFormatError(
    expectedFragment: String,
    load: () -> Unit,
): EffectFormatException {
    val error = try {
        load()
        throw AssertionError("Expected format validation to fail")
    } catch (thrown: EffectFormatException) {
        thrown
    }
    val message = error.message.orEmpty()
    if (!message.contains(expectedFragment)) {
        throw AssertionError("Expected message containing '$expectedFragment', but was '$message'")
    }
    return error
}
