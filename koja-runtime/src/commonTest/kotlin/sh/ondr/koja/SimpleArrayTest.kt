package sh.ondr.koja

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleArrayTest {
	val json = Json { prettyPrint = true }

	@Test
	fun testArrayOfPrimitives() {
		@Serializable
		data class PrimitiveArrayHolder(
			val names: List<String>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "names": {
				      "type": "array",
				      "items": {
				        "type": "string"
				      }
				    }
				  },
				  "required": ["names"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<PrimitiveArrayHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfNumbersWithDefault() {
		@Serializable
		data class NumbersWithDefault(
			val values: List<Int> = listOf(1, 2, 3),
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "values": {
				      "type": "array",
				      "items": {
				        "type": "number"
				      }
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<NumbersWithDefault>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testOptionalArrayField() {
		@Serializable
		data class OptionalArray(
			val tags: List<String>? = null,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "tags": {
				      "type": "array",
				      "items": {
				        "type": "string"
				      }
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<OptionalArray>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfNullableElements() {
		@Serializable
		data class NullableElementsArray(
			val items: List<String?>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "items": {
				      "type": "array",
				      "items": {
				        "type":"string"
				      }
				    }
				  },
				  "required": ["items"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<NullableElementsArray>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfObjects() {
		@Serializable
		data class Person(val name: String, val age: Int)

		@Serializable
		data class PeopleHolder(
			val people: List<Person>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "people": {
				      "type": "array",
				      "items": {
				        "type": "object",
				        "properties": {
				          "name": {"type":"string"},
				          "age": {"type":"number"}
				        },
				        "required":["name","age"]
				      }
				    }
				  },
				  "required": ["people"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<PeopleHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Serializable
	enum class SimpleColor { RED, GREEN, BLUE }

	@Test
	fun testArrayOfEnums() {
		@Serializable
		data class ColorPalette(val colors: List<SimpleColor>)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "colors": {
				      "type": "array",
				      "items": {
				        "type": "string",
				        "enum": ["RED","GREEN","BLUE"]
				      }
				    }
				  },
				  "required":["colors"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ColorPalette>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayWithSerialName() {
		@Serializable
		data class RenamedField(
			@kotlinx.serialization.SerialName("user_ids") val userIds: List<Int>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "user_ids": {
				      "type": "array",
				      "items": {
				        "type": "number"
				      }
				    }
				  },
				  "required":["user_ids"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<RenamedField>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfMaps() {
		@Serializable
		data class MapHolder(val dicts: List<Map<String, Int>>)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "dicts": {
				      "type": "array",
				      "items": {
				        "type":"object",
				        "additionalProperties": {"type":"number"}
				      }
				    }
				  },
				  "required":["dicts"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<MapHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Serializable
	sealed class SimpleShape {
		@Serializable
		data class Circle(val radius: Double) : SimpleShape()

		@Serializable
		data class Rectangle(val width: Double, val height: Double) : SimpleShape()
	}

	@Test
	fun testArrayOfSealedClass() {
		@Serializable
		data class ShapesHolder(val shapes: List<SimpleShape>)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties": {
				    "shapes": {
				      "type":"array",
				      "items": {
				        "type":"object"
				      }
				    }
				  },
				  "required":["shapes"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ShapesHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayWithDefaultsAndNullabilityMixed() {
		@Serializable
		data class MixedArray(
			val strings: List<String>? = listOf("defaultStr"),
			val numbers: List<Int> = emptyList(),
			val optionalNullable: List<Int?>? = null,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties": {
				    "strings": {
				      "type":"array",
				      "items": {"type":"string"}
				    },
				    "numbers": {
				      "type":"array",
				      "items": {"type":"number"}
				    },
				    "optionalNullable": {
				      "type":"array",
				      "items":{"type":"number"}
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<MixedArray>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfArrays() {
		@Serializable
		data class NestedArrays(
			val matrix: List<List<String>>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties": {
				    "matrix": {
				      "type":"array",
				      "items":{
				        "type":"array",
				        "items":{"type":"string"}
				      }
				    }
				  },
				  "required":["matrix"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<NestedArrays>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfObjectsWithOptionalFields() {
		@Serializable
		data class OptionalFieldsObj(
			val maybe: String? = null,
			val defaultNum: Int = 42,
		)

		@Serializable
		data class OptionalObjHolder(
			val elements: List<OptionalFieldsObj>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "elements":{
				      "type":"array",
				      "items":{
				        "type":"object",
				        "properties":{
				          "maybe":{"type":"string"},
				          "defaultNum":{"type":"number"}
				        }
				      }
				    }
				  },
				  "required":["elements"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<OptionalObjHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}
}
