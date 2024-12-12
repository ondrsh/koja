@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.jsonschema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.serializer

/**
 * Generates a JSON schema-like representation of the specified Kotlin type [T].
 *
 * This function uses Kotlinx Serialization's [serializer] and [SerialDescriptor] API at runtime
 * to analyze the structure of the given type [T], which must be annotated with `@Serializable`.
 * The resulting JSON schema object is inspired by the JSON Schema Specification and related formats
 * (e.g., OpenAPI), and follows common keys such as `"type"`, `"properties"`, `"items"`, `"required"`,
 * `"enum"`, and `"additionalProperties"`.
 *
 * The generated schema tries to be as close as possible to a standard JSON Schema representation
 * while remaining simple and general. For instance:
 * - For classes, it produces an `"object"` schema with `"properties"` and `"required"` fields.
 * - For primitive fields, it infers `"type"` as `"string"`, `"boolean"`, or `"number"`.
 * - For lists, it generates `"type": "array"` with `"items"` containing the item schema.
 * - For maps, it uses `"type": "object"` with `"additionalProperties"` describing the value schema.
 * - For enums, it produces `"enum"` listing all possible string values.
 * - Polymorphic and contextual types currently fall back to `"object"` with limited detail.
 *
 * **Example:**
 * ```kotlin
 * @Serializable
 * data class Person(val name: String, val age: Int)
 *
 * val schema = jsonSchema<Person>()
 * // schema might look like:
 * // {
 * //   "type": "object",
 * //   "properties": {
 * //     "name": { "type": "string" },
 * //     "age": { "type": "number" }
 * //   },
 * //   "required": ["name", "age"]
 * // }
 * ```
 *
 * Note that the output is a `JsonObject` suitable for further processing, exporting as a JSON string,
 * or feeding into tools that expect JSON schemas. However, it is not a fully compliant JSON Schema
 * validator or generator. Additional refinement may be needed for advanced use cases.
 *
 * @return A [JsonObject] representing the schema of [T].
 * @throws NotImplementedError If certain serial kinds (like `SerialKind.CONTEXTUAL`) are encountered
 *         and not yet supported.
 */
inline fun <reified T> jsonSchema(): JsonObject = descriptorToSchema(serializer<T>().descriptor)

fun descriptorToSchema(descriptor: SerialDescriptor): JsonObject =
	buildJsonObject {
		when (descriptor.kind) {
			is PrimitiveKind -> handlePrimitiveDescriptor(descriptor.kind as PrimitiveKind)
			StructureKind.CLASS, StructureKind.OBJECT -> handleClassDescriptor(descriptor)
			StructureKind.LIST -> handleArrayDescriptor(descriptor)
			StructureKind.MAP -> handleMapDescriptor(descriptor)
			PolymorphicKind.OPEN, PolymorphicKind.SEALED -> handlePolymorphicDescriptor()
			SerialKind.ENUM -> handleEnumDescriptor(descriptor)
			SerialKind.CONTEXTUAL -> TODO()
		}
	}

// Handle regular @Serializable classes
private fun JsonObjectBuilder.handleClassDescriptor(descriptor: SerialDescriptor) {
	put("type", "object")

	val requiredFields = mutableListOf<String>()
	putJsonObject("properties") {
		descriptor.elementDescriptors.forEachIndexed { i, elementDescriptor ->
			val elementName = descriptor.getElementName(i)
			val elementSchema = descriptorToSchema(elementDescriptor)
			put(elementName, elementSchema)
			val elementRequired = descriptor.childRequired(elementDescriptor, i)
			if (elementRequired) {
				requiredFields += elementName
			}
		}
	}

	if (requiredFields.isNotEmpty()) {
		putJsonArray("required") {
			addAll(requiredFields)
		}
	}
}

private fun JsonObjectBuilder.handleArrayDescriptor(descriptor: SerialDescriptor) {
	put("type", "array")
	val itemDescriptor = descriptor.getElementDescriptor(0)
	put("items", descriptorToSchema(itemDescriptor))
}

private fun JsonObjectBuilder.handleMapDescriptor(descriptor: SerialDescriptor) {
	put("type", "object")
	val valueDescriptor = descriptor.getElementDescriptor(1)
	put("additionalProperties", descriptorToSchema(valueDescriptor))
}

private fun JsonObjectBuilder.handlePolymorphicDescriptor() {
	// TODO Polymorphic types are not yet supported. Falling back to 'object' type
	put("type", "object")
}

private fun JsonObjectBuilder.handlePrimitiveDescriptor(kind: PrimitiveKind) {
	val typeStr =
		when (kind) {
			PrimitiveKind.STRING -> "string"
			PrimitiveKind.BOOLEAN -> "boolean"
			PrimitiveKind.CHAR -> "string"
			PrimitiveKind.BYTE,
			PrimitiveKind.SHORT,
			PrimitiveKind.INT,
			PrimitiveKind.LONG,
			PrimitiveKind.FLOAT,
			PrimitiveKind.DOUBLE,
			-> "number"
		}
	put("type", typeStr)
}

private fun JsonObjectBuilder.handleEnumDescriptor(descriptor: SerialDescriptor) {
	put("type", "string")
	putJsonArray("enum") {
		addAll(descriptor.elementNames)
	}
}
