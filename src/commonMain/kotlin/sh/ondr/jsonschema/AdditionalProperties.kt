package sh.ondr.jsonschema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AdditionalProperties {
	@Serializable
	@SerialName("true")
	object True : AdditionalProperties()

	@Serializable
	@SerialName("false")
	object False : AdditionalProperties()

	@Serializable
	class Schema(val schema: JsonSchema) : AdditionalProperties()
}
