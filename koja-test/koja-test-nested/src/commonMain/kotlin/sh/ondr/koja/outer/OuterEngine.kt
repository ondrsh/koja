package sh.ondr.koja.outer

import kotlinx.serialization.Serializable
import sh.ondr.koja.JsonSchema

/**
 * A class representing an engine
 *
 * @property name The name of the engine.
 */
@Serializable @JsonSchema
data class OuterEngine(
	val name: String,
)
