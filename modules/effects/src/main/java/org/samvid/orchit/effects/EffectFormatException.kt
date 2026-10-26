package org.samvid.orchit.effects

/** Raised when an effects format document is malformed or violates its version 1 contract. */
class EffectFormatException(message: String) : IllegalArgumentException(message)
