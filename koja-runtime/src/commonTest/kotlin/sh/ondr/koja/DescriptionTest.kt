@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.koja

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionTest {
	@Serializable @JsonSchema
	data class PersonWithDoc(
		val name: String,
		val age: Int,
	)

	@Test
	fun testObjectAndPropertyDescriptions() {
		KojaRegistry.map[serialDescriptor<PersonWithDoc>().serialName] = KojaMeta(
			description = "A class representing a person.",
			parameterDescriptions = mapOf(
				"name" to "The name of the person.",
				"age" to "The age of the person.",
			),
		)

		val actualSchema = jsonSchema<PersonWithDoc>().toJsonElement()

		val expectedSchema = Json.parseToJsonElement(
			"""
			{
			  "type": "object",
			  "description": "A class representing a person.",
			  "properties": {
			    "name": {
			      "type": "string",
			      "description": "The name of the person."
			    },
			    "age": {
			      "type": "number",
			      "description": "The age of the person."
			    }
			  },
			  "required": ["name", "age"]
			}
			""".trimIndent(),
		).jsonObject

		assertEquals(expectedSchema, actualSchema)
	}
}
