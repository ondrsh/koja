package sh.ondr.koja

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
@JsonSchema
data class RecursiveNode(
	val name: String,
	val next: RecursiveNode?,
)

class RecursiveTest {
	@Test
	fun testRecursiveClassDoesNotOverflow() {
		val expectedSchema = Json
			.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "name": { "type": "string" },
				    "next": { "type": "object" }
				  },
				  "required": ["name"]
				}
				""".trimIndent(),
			).jsonObject

		assertEquals(expectedSchema, jsonSchema<RecursiveNode>().toJsonElement())
	}
}
