@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.jsonschema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializer
import sh.ondr.jsonschema.JsonSchema.ArraySchema
import sh.ondr.jsonschema.JsonSchema.BooleanSchema
import sh.ondr.jsonschema.JsonSchema.NumberSchema
import sh.ondr.jsonschema.JsonSchema.ObjectSchema
import sh.ondr.jsonschema.JsonSchema.StringSchema

/**
 * Generate a JSON Schema representation of the specified Kotlin type [T].
 *
 * This function inspects the Kotlinx Serialization [serializer] and its [SerialDescriptor]
 * to produce a corresponding JSON Schema model. The resulting schema is expressed as a hierarchy
 * of [JsonSchema] subclasses, and can be serialized to JSON via `jsonObject`.
 *
 * By default:
 * - Primitive Kotlin types map to standard JSON Schema types ("string", "number", "boolean").
 * - Classes and objects become "object" schemas, with `properties` and `required` fields inferred from
 *   the class structure and nullability.
 * - Lists become "array" schemas with an `items` field.
 * - Enums are represented as string schemas with an `enum` array.
 * - Maps are expressed as objects with `additionalProperties` referencing a subschema for the values.
 *
 * @return A [JsonSchema] instance representing the structure and types of [T].
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Person(val name: String, val age: Int)
 *
 * val schema = jsonSchema<Person>()
 * println(schema.jsonObject) // Prints a JSON Schema as a JsonElement
 * ```
 */
inline fun <reified T> jsonSchema(): JsonSchema = serializer<T>().descriptor.toSchema()

fun SerialDescriptor.toSchema(): JsonSchema =
	when (kind) {
		is PrimitiveKind -> handlePrimitiveDescriptor(kind as PrimitiveKind)
		StructureKind.CLASS, StructureKind.OBJECT -> toObjectSchema()
		StructureKind.LIST -> toArraySchema()
		StructureKind.MAP -> handleMapDescriptor()
		PolymorphicKind.OPEN, PolymorphicKind.SEALED -> handlePolymorphicDescriptor()
		SerialKind.ENUM -> handleEnumDescriptor()
		SerialKind.CONTEXTUAL -> TODO("Handle contextual")
	}

private fun SerialDescriptor.toObjectSchema(): JsonSchema {
	val properties = mutableMapOf<String, JsonSchema>()
	val requiredFields = mutableListOf<String>()

	(0 until elementsCount).forEach { i ->
		val elementName = getElementName(i)
		properties[elementName] = getElementDescriptor(i).toSchema()

		if (childRequired(i)) {
			requiredFields += elementName
		}
	}

	return ObjectSchema(
		properties = properties,
		required = requiredFields.ifEmpty { null },
	)
}

private fun SerialDescriptor.toArraySchema(): JsonSchema {
	// Lists have a single element descriptor for items
	val itemSchema = getElementDescriptor(0).toSchema()
	return ArraySchema(items = itemSchema)
}

// Handle maps as objects with additionalProperties
private fun SerialDescriptor.handleMapDescriptor(): JsonSchema {
	val valueDescriptor = getElementDescriptor(1)
	return ObjectSchema(additionalProperties = valueDescriptor.toSchema().jsonObject)
}

// Polymorphic fallback: treat as object for now
private fun SerialDescriptor.handlePolymorphicDescriptor(): JsonSchema {
	return ObjectSchema()
}

private fun handlePrimitiveDescriptor(kind: PrimitiveKind): JsonSchema =
	when (kind) {
		PrimitiveKind.STRING, PrimitiveKind.CHAR -> StringSchema()
		PrimitiveKind.BOOLEAN -> BooleanSchema()
		PrimitiveKind.BYTE,
		PrimitiveKind.SHORT,
		PrimitiveKind.INT,
		PrimitiveKind.LONG,
		PrimitiveKind.FLOAT,
		PrimitiveKind.DOUBLE,
		-> NumberSchema()
	}

// Enums are just string schema with their enum values as array
private fun SerialDescriptor.handleEnumDescriptor(): JsonSchema {
	return StringSchema(enum = elementNames.toList())
}
