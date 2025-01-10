@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.koja

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializer
import sh.ondr.koja.Schema.ArraySchema
import sh.ondr.koja.Schema.BooleanSchema
import sh.ondr.koja.Schema.NumberSchema
import sh.ondr.koja.Schema.ObjectSchema
import sh.ondr.koja.Schema.StringSchema

/**
 * Returns a JSON Schema representation of the specified type [T].
 *
 * This function analyzes the serialization metadata of the given `@Serializable` class [T]
 * and produces a corresponding [Schema]. The returned schema describes the structure and
 * constraints of [T] in terms of JSON Schema types.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class Person(
 *     val name: String,
 *     val age: Double?,
 *     val emails: List<String> = emptyList(),
 * )
 *
 * val schema = jsonSchema<Person>()
 * println(schema.jsonObject)
 * ```
 *
 * Output:
 * ```json
 * {
 *   "type": "object",
 *   "properties": {
 *     "name": {
 *       "type": "string"
 *     },
 *     "age": {
 *       "type": "number"
 *     },
 *     "emails": {
 *       "type": "array",
 *       "items": {
 *         "type": "string"
 *       }
 *     }
 *   },
 *   "required": ["name"]
 * }
 * ```
 *
 * Here, `name` is required because it is non-nullable and has no default value, while
 * `age` is optional (because it is nullable) and so is 'emails' (because it has a default value).
 *
 * [T] must be annotated with `@Serializable`. Otherwise, this function will fail at runtime.
 */
inline fun <reified T : @Serializable Any> jsonSchema(): Schema = serializer<T>().descriptor.toSchema()

@PublishedApi
internal fun SerialDescriptor.toSchema(): Schema =
	when (kind) {
		is PrimitiveKind -> toPrimitiveSchema(kind as PrimitiveKind)
		StructureKind.CLASS, StructureKind.OBJECT -> toObjectSchema()
		StructureKind.LIST -> toArraySchema()
		StructureKind.MAP -> toMapSchema()
		PolymorphicKind.OPEN, PolymorphicKind.SEALED -> toPolymorphicSchema()
		SerialKind.ENUM -> toEnumSchema()
		SerialKind.CONTEXTUAL -> TODO("Handle contextual")
	}

internal fun SerialDescriptor.toObjectSchema(): Schema {
	val properties = mutableMapOf<String, Schema>()
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

internal fun SerialDescriptor.toArraySchema(): Schema {
	// Lists have a single element descriptor for items
	val itemDescriptor = getElementDescriptor(0)
	return ArraySchema(items = itemDescriptor.toSchema())
}

// Handle maps as objects with additionalProperties
internal fun SerialDescriptor.toMapSchema(): Schema {
	val valueDescriptor = getElementDescriptor(1)
	return ObjectSchema(additionalProperties = valueDescriptor.toSchema().toJsonElement())
}

// Polymorphic fallback: let's just return object schema for now
internal fun SerialDescriptor.toPolymorphicSchema(): Schema {
	return ObjectSchema()
}

internal fun toPrimitiveSchema(kind: PrimitiveKind): Schema =
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
internal fun SerialDescriptor.toEnumSchema(): Schema {
	return StringSchema(enum = elementNames.toList())
}

// A field is considered required if it is neither optional nor nullable.
// This aligns with the library’s opinionated definition of "required".
internal fun SerialDescriptor.childRequired(index: Int): Boolean {
	return isElementOptional(index).not() && getElementDescriptor(index).isNullable.not()
}
