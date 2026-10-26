package org.samvid.orchit.effects

/** A named parameter value supported by an effect. */
data class EffectParameter(val name: String, val value: String)
/** Effect application bound to a timeline object. */
data class AppliedEffect(val effectId: String, val targetId: String, val parameters: List<EffectParameter>)
/** Transition between adjacent timeline objects. */
data class Transition(val transitionId: String, val durationUs: Long, val parameters: List<EffectParameter>)
/** Describes an effect that can be selected by editor clients. */
interface EffectCatalog { fun effectIds(): List<String>; fun validate(effect: AppliedEffect): Boolean }
/** Applies color transforms and visual effects during rendering. */
interface VisualProcessor { fun apply(effect: AppliedEffect); fun transition(transition: Transition) }
