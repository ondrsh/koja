package sh.ondr.jsonschema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
enum class AdvancedColor {
	CYAN,
	MAGENTA,
	YELLOW,
	BLACK,
}

@Serializable
sealed class AdvancedShape {
	@Serializable
	@SerialName("poly_circle")
	data class PolyCircle(val radius: Double) : AdvancedShape()

	@Serializable
	@SerialName("poly_triangle")
	data class PolyTriangle(val base: Double, val height: Double) : AdvancedShape()
}

class AdvancedArrayTest {
	@Test
	fun testDeeplyNestedAndMixedArray() {
		// A structure that nests arrays several levels deep and mixes in maps.
		// data: List<Map<String, List<List<Int>>>>
		// For instance: data -> array -> map -> key:string, value: array -> array -> int
		@Serializable
		data class DeepMixed(
			val data: List<Map<String, List<List<Int>>>>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "data": {
				      "type": "array",
				      "items": {
				        "type": "object",
				        "additionalProperties": {
				          "type": "array",
				          "items": {
				            "type":"array",
				            "items": { "type":"number" }
				          }
				        }
				      }
				    }
				  },
				  "required":["data"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<DeepMixed>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfPolymorphicShapesWithRenames() {
		// Arrays of sealed classes. Each item is polymorphic but we only get "type":"object".
		// We test @SerialName on the sealed subclasses to see if anything changes (it won't, but good to ensure stability).
		@Serializable
		data class ShapeCollection(val shapes: List<AdvancedShape>)

		// Polymorphic fallback: items remain { "type":"object" } with no further detail.
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties": {
				    "shapes": {
				      "type":"array",
				      "items": { "type":"object" }
				    }
				  },
				  "required":["shapes"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ShapeCollection>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfArraysOfEnums() {
		// Even more complex enums: arrays of arrays of AdvancedColor
		@Serializable
		data class ColorGrid(
			val grid: List<List<AdvancedColor>>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "grid":{
				      "type":"array",
				      "items":{
				        "type":"array",
				        "items":{
				          "type":"string",
				          "enum":["CYAN","MAGENTA","YELLOW","BLACK"]
				        }
				      }
				    }
				  },
				  "required":["grid"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<ColorGrid>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayWithMultipleOptionalLayers() {
		// A scenario where arrays are optional, have defaults, and contain nullable elements.
		@Serializable
		data class MultiOptionArrays(
			val optionalNested: List<List<String>>? = listOf(listOf("default")),
			val nullableContent: List<List<String?>> = listOf(listOf(null, "text")),
			val defaultEmpty: List<Int> = emptyList(),
		)

		// optionalNested has a default, not required.
		// nullableContent no default? Actually it has a default (listOf(listOf(null, "text"))), so not required.
		// defaultEmpty has a default empty list, not required.
		// Everything ends up optional due to defaults.
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "optionalNested":{
				      "type":"array",
				      "items":{
				        "type":"array",
				        "items":{
				          "type":"string"
				        }
				      }
				    },
				    "nullableContent":{
				      "type":"array",
				      "items":{
				        "type":"array",
				        "items":{
				          "type":"string"
				        }
				      }
				    },
				    "defaultEmpty":{
				      "type":"array",
				      "items":{
				        "type":"number"
				      }
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<MultiOptionArrays>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfObjectsThatContainArraysOfMaps() {
		// Very nested scenario:
		// container: List<AdvancedItem>
		// AdvancedItem: name:String, metrics:List<Map<String,Double>>
		@Serializable
		data class AdvancedItem(
			val name: String,
			val metrics: List<Map<String, Double>>,
		)

		@Serializable
		data class AdvancedContainer(val container: List<AdvancedItem>)

		// metrics: array of object maps: key=string, value=number
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "container":{
				      "type":"array",
				      "items":{
				        "type":"object",
				        "properties":{
				          "name":{"type":"string"},
				          "metrics":{
				            "type":"array",
				            "items":{
				              "type":"object",
				              "additionalProperties":{"type":"number"}
				            }
				          }
				        },
				        "required":["name","metrics"]
				      }
				    }
				  },
				  "required":["container"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<AdvancedContainer>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfRenamedPropertiesAndEnumsInObjects() {
		// Introduce @SerialName on array field and enum field:
		@Serializable
		data class RenamedEnumHolder(
			@SerialName("color_values")
			val colors: List<AdvancedColor>,
		)

		// colors is required
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "color_values":{
				      "type":"array",
				      "items":{
				        "type":"string",
				        "enum":["CYAN","MAGENTA","YELLOW","BLACK"]
				      }
				    }
				  },
				  "required":["color_values"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<RenamedEnumHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfPolymorphicWithDefaultsAndNullable() {
		// Sealed class in array, optional and default, plus nullable inside the shape somehow?
		@Serializable
		data class PolymorphicContainer(
			val shapes: List<AdvancedShape>? = listOf(AdvancedShape.PolyCircle(1.0)),
		)

		// shapes has a default, so not required.
		// Polymorphic items still "object"
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "shapes":{
				      "type":"array",
				      "items":{"type":"object"}
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<PolymorphicContainer>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testArrayOfObjectsWhereInnerArraysAreOptional() {
		// Here we have an object with optional arrays inside it.
		@Serializable
		data class InnerArrays(
			val codes: List<String>? = null,
			val levels: List<Int>? = listOf(1, 2),
			val tags: List<String> = emptyList(),
		)

		@Serializable
		data class MultiArraysHolder(val items: List<InnerArrays> = listOf())

		// Items has default empty list, not required
		// Inside items:
		// - codes: default null -> not required
		// - levels: default listOf(1,2) -> not required
		// - tags: default empty -> not required
		// So no required fields in the item schema.
		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "items":{
				      "type":"array",
				      "items":{
				        "type":"object",
				        "properties":{
				          "codes":{
				            "type":"array",
				            "items":{"type":"string"}
				          },
				          "levels":{
				            "type":"array",
				            "items":{"type":"number"}
				          },
				          "tags":{
				            "type":"array",
				            "items":{"type":"string"}
				          }
				        }
				      }
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<MultiArraysHolder>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}

	@Test
	fun testCrazyNestedArraysWithEnumsAndMaps() {
		// Extremely deep nesting:
		// data: List<List<Map<String, List<AdvancedColor>>>>
		@Serializable
		data class CrazyNested(
			val data: List<List<Map<String, List<AdvancedColor>>>>,
		)

		val expectedSchema =
			Json.parseToJsonElement(
				"""
				{
				  "type":"object",
				  "properties":{
				    "data":{
				      "type":"array",
				      "items":{
				        "type":"array",
				        "items":{
				          "type":"object",
				          "additionalProperties":{
				            "type":"array",
				            "items":{
				              "type":"string",
				              "enum":["CYAN","MAGENTA","YELLOW","BLACK"]
				            }
				          }
				        }
				      }
				    }
				  },
				  "required":["data"]
				}
				""".trimIndent(),
			).jsonObject

		val actualSchema = jsonSchema<CrazyNested>()
		assertEquals(expectedSchema, actualSchema.toJsonElement())
	}
}
