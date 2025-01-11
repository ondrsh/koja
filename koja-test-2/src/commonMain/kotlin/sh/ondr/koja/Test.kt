package sh.ondr.koja

import kotlinx.serialization.Serializable

/**
 * A class representing an engine
 *
 * @property name The name of the engine.
 */
@Serializable @JsonSchema
data class Engine(
	val name: String,
)
