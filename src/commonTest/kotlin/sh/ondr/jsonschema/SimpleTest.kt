package sh.ondr.jsonschema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleTest {
	@Test
	fun testSimpleNameClassSchema() {
		@Serializable
		data class SimpleNameClass(
			val name: String,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "name": {
				      "type": "string"
				    }
				  },
				  "required": ["name"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<SimpleNameClass>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Test
	fun testPrimitiveTypesSchema() {
		@Serializable
		data class PrimitiveTypes(
			val str: String,
			val bool: Boolean,
			val intVal: Int,
			val doubleVal: Double,
			val longVal: Long,
			val charVal: Char,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "str": { "type": "string" },
				    "bool": { "type": "boolean" },
				    "intVal": { "type": "number" },
				    "doubleVal": { "type": "number" },
				    "longVal": { "type": "number" },
				    "charVal": { "type": "string" }
				  },
				  "required": ["str", "bool", "intVal", "doubleVal", "longVal", "charVal"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<PrimitiveTypes>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Test
	fun testOptionalFieldsSchema() {
		@Serializable
		data class OptionalFields(
			val mandatory: String,
			val optionalString: String? = null,
			val defaultInt: Int = 42,
			val nullableInt: Int?,
		)

		// "nullableInt" is also optional (no default, but nullable)
		// "optionalString", "nullableInt" and "defaultInt" should not be required
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "mandatory": { "type": "string" },
				    "optionalString": { "type": "string" },
				    "defaultInt": { "type": "number" },
				    "nullableInt": { "type": "number" }
				  },
				  "required": ["mandatory"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<OptionalFields>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Test
	fun testNestedObjectsSchema() {
		@Serializable
		data class Address(val city: String, val zip: Int)

		@Serializable
		data class Person(val name: String, val address: Address)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "name": { "type": "string" },
				    "address": {
				      "type": "object",
				      "properties": {
				        "city": { "type": "string" },
				        "zip": { "type": "number" }
				      },
				      "required": ["city", "zip"]
				    }
				  },
				  "required": ["name", "address"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<Person>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Test
	fun testListsSchema() {
		@Serializable
		data class ListsExample(
			val strings: List<String>,
			val numbers: List<Int>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "strings": {
				      "type": "array",
				      "items": { "type": "string" }
				    },
				    "numbers": {
				      "type": "array",
				      "items": { "type": "number" }
				    }
				  },
				  "required": ["strings", "numbers"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ListsExample>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Test
	fun testMapsSchema() {
		@Serializable
		data class MapExample(
			val dict: Map<String, Int>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "dict": {
				      "type": "object",
				      "additionalProperties": { "type": "number" }
				    }
				  },
				  "required": ["dict"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<MapExample>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Serializable
	enum class Color { RED, GREEN, BLUE }

	@Test
	fun testEnumSchema() {
		@Serializable
		data class EnumHolder(val color: Color)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "color": {
				      "type": "string",
				      "enum": ["RED", "GREEN", "BLUE"]
				    }
				  },
				  "required": ["color"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<EnumHolder>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Serializable
	sealed class Shape {
		@Serializable
		data class Circle(val radius: Double) : Shape()

		@Serializable
		data class Rectangle(val width: Double, val height: Double) : Shape()
	}

	@Test
	fun testSealedClassSchema() {
		@Serializable
		data class ShapeHolder(val shape: Shape)

		// Just represent polymorphic as a generic "object" for now
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "shape": {
				      "type": "object"
				    }
				  },
				  "required": ["shape"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ShapeHolder>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Test
	fun testFullyOptionalClass() {
		@Serializable
		data class FullyOptional(
			val maybeStr: String? = null,
			val maybeInt: Int? = null,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "maybeStr": { "type": "string" },
				    "maybeInt": { "type": "number" }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<FullyOptional>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}

	@Serializable
	enum class Status { ACTIVE, INACTIVE }

	@Test
	fun testComplexMixedClass() {
		@Serializable
		data class Credentials(val user: String, val token: String?)

		@Serializable
		data class ComplexClass(
			val name: String,
			val age: Int,
			val status: Status = Status.ACTIVE,
			val tags: List<String>?,
			val attributes: Map<String, Int> = mapOf("default" to 42),
			val credentials: Credentials?,
		)

		// name, age, and credentials are required (no defaults, not nullable)
		// status has a default (so not required)
		// tags is nullable (not required)
		// attributes has a default map, so not required
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "name": { "type": "string" },
				    "age": { "type": "number" },
				    "status": {
				      "type": "string",
				      "enum": ["ACTIVE", "INACTIVE"]
				    },
				    "tags": {
				      "type": "array",
				      "items": { "type": "string" }
				    },
				    "attributes": {
				      "type": "object",
				      "additionalProperties": { "type": "number" }
				    },
				    "credentials": {
				      "type": "object",
				      "properties": {
				        "user": { "type": "string" },
				        "token": { "type": "string" }
				      },
				      "required": ["user"]
				    }
				  },
				  "required": ["name", "age"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ComplexClass>()
		assertEquals(expectedSchema, actualSchema.jsonObject)
	}
}
