package sh.ondr.jsonschema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class SimpleArrayClass(
	val names: List<String>,
)

class ArrayTest {
	@Test
	fun testSimpleArrayClassSchema() {
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

		val actualSchema = jsonSchema<SimpleArrayClass>()
		assertEquals(expectedSchema, actualSchema)
	}
}
