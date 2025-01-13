@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.koja

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
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
inline fun <reified T : @JsonSchema Any> jsonSchema(): Schema = serializer<T>().descriptor.toSchema()

fun SerialDescriptor.toSchema(): Schema {
	return when (kind) {
		is PrimitiveKind -> toPrimitiveSchema(kind as PrimitiveKind)
		StructureKind.CLASS, StructureKind.OBJECT -> toObjectSchema()
		StructureKind.LIST -> toArraySchema()
		StructureKind.MAP -> toMapSchema()
		PolymorphicKind.OPEN, PolymorphicKind.SEALED -> toPolymorphicSchema()
		SerialKind.ENUM -> toEnumSchema()
		SerialKind.CONTEXTUAL -> TODO("Handle contextual")
	}
}

internal fun SerialDescriptor.toObjectSchema(): Schema {
	require(annotations.any { it is JsonSchema }) {
		"Schema generation requires the @JsonSchema annotation on the class"
	}
	val properties = mutableMapOf<String, Schema>()
	val requiredFields = mutableListOf<String>()

	val meta = KojaRegistry.map[serialName]

	for (i in 0 until elementsCount) {
		val elementName = getElementName(i)
		val childDescriptor = getElementDescriptor(i)
		val childSchema = childDescriptor.toSchema()
		val paramDesc = meta?.parameterDescriptions?.get(elementName)
		val propertyAnnotations = getElementAnnotations(i)
		val serialNameAnnotation = propertyAnnotations.filterIsInstance<SerialName>().firstOrNull()
		if (serialNameAnnotation != null) {
			println("Property $i has @SerialName = ${serialNameAnnotation.value}")
		}

		properties[elementName] = if (paramDesc != null) {
			childSchema.withDescription(paramDesc)
		} else {
			childSchema
		}

		// Decide if it’s required
		if (!isElementOptional(i) && !childDescriptor.isNullable) {
			requiredFields += elementName
		}
	}

	return ObjectSchema(
		// the class-level description from KojaMeta
		description = meta?.description,
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

internal fun Schema.withDescription(desc: String): Schema =
	when (this) {
		is ObjectSchema -> copy(description = desc)
		is StringSchema -> copy(description = desc)
		is NumberSchema -> copy(description = desc)
		is ArraySchema -> copy(description = desc)
		is BooleanSchema -> copy(description = desc)
	}
