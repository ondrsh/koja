package sh.ondr.koja

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Provides a default configuration for encoding [Schema] instances into JSON.
 */
object SchemaEncoder {
	val format = Json {
		encodeDefaults = true
		explicitNulls = false
	}
}

/**
 * Converts this [Schema] instance into a [JsonElement] using [SchemaEncoder].
 */
fun Schema.toJsonElement(): JsonElement = SchemaEncoder.format.encodeToJsonElement(this)

/**
 * Represents a JSON Schema definition.
 *
 * This sealed class and its subclasses model different types in JSON Schema:
 *
 * - [ObjectSchema]: Represents a JSON object with defined properties.
 * - [StringSchema]: Represents a string type, possibly with an enumeration of allowed values.
 * - [NumberSchema]: Represents a numeric type (integers, floats, doubles).
 * - [ArraySchema]: Represents an array type, specifying items as another schema.
 * - [BooleanSchema]: Represents a boolean type.
 */
@Serializable
sealed class Schema {
	abstract val description: String?

	@Serializable
	@SerialName("object")
	data class ObjectSchema(
		override val description: String? = null,
		val properties: Map<String, Schema>? = null,
		val required: List<String>? = null,
		val additionalProperties: JsonElement? = null,
	) : Schema()

	@Serializable
	@SerialName("string")
	data class StringSchema(
		override val description: String? = null,
		val enum: List<String>? = null,
	) : Schema()

	@Serializable
	@SerialName("number")
	data class NumberSchema(
		override val description: String? = null,
	) : Schema()

	@Serializable
	@SerialName("array")
	data class ArraySchema(
		override val description: String? = null,
		val items: Schema,
	) : Schema()

	@Serializable
	@SerialName("boolean")
	data class BooleanSchema(
		override val description: String? = null,
	) : Schema()
}
