package sh.ondr.jsonschema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

// Polymorphic serialization will use "type" as discriminator by default, which is why
// each subclass specifies a @SerialName that matches the desired "type" keyword value.
@Serializable
sealed class JsonSchema {
	val jsonObject by lazy { json.encodeToJsonElement(this) }

	companion object {
		val json = Json { encodeDefaults = false }
	}

	@Serializable
	@SerialName("object") // "type":"object"
	class ObjectSchema(
		val properties: Map<String, JsonSchema> = emptyMap(),
		val required: List<String> = emptyList(),
		val additionalProperties: JsonElement? = null,
	) : JsonSchema()

	@Serializable
	@SerialName("string") // "type":"string"
	class StringSchema(val enum: List<String>? = null) : JsonSchema()

	@Serializable
	@SerialName("number") // "type":"number"
	class NumberSchema() : JsonSchema()

	@Serializable
	@SerialName("array") // "type":"array"
	class ArraySchema(val items: JsonSchema) : JsonSchema()

	@Serializable
	@SerialName("boolean") // "type":"boolean"
	class BooleanSchema() : JsonSchema()
}
