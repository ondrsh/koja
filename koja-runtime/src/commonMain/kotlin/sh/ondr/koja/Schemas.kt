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
	/**
	 * Represents an "object" type schema, describing its properties, required fields, and optionally
	 * additional properties.
	 */
	@Serializable
	@SerialName("object")
	class ObjectSchema(
		val properties: Map<String, Schema>? = null,
		val required: List<String>? = null,
		val additionalProperties: JsonElement? = null,
	) : Schema()

	/**
	 * Represents a "string" type schema. The [enum] field, if present, restricts the string to a
	 * predefined set of values.
	 */
	@Serializable
	@SerialName("string")
	class StringSchema(val enum: List<String>? = null) : Schema()

	/**
	 * Represents a "number" type schema. In JSON Schema, "number" covers both integers and floating
	 * point numbers.
	 */
	@Serializable
	@SerialName("number")
	class NumberSchema() : Schema()

	/**
	 * Represents an "array" type schema. The [items] field specifies the schema for elements in the array.
	 */
	@Serializable
	@SerialName("array")
	class ArraySchema(val items: Schema) : Schema()

	/**
	 * Represents a "boolean" type schema.
	 */
	@Serializable
	@SerialName("boolean")
	class BooleanSchema() : Schema()
}
