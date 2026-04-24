@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.koja

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
@JsonSchema
data class NullableChild(
	val value: String,
)

@Serializable
@JsonSchema
data class NullableChildHolder(
	val child: NullableChild?,
)

class NullableRegistryTest {
	@Test
	fun testNullableObjectUsesRegistryMetadata() {
		KojaRegistry.map[serialDescriptor<NullableChild>().serialName] = KojaMeta(
			description = "A nullable child.",
			parameterDescriptions = mapOf("value" to "The child value."),
		)
		KojaRegistry.map[serialDescriptor<NullableChildHolder>().serialName] = KojaMeta(
			description = null,
			parameterDescriptions = mapOf("child" to "The optional child."),
		)

		val expectedSchema = Json
			.parseToJsonElement(
				"""
				{
				  "type": "object",
				  "properties": {
				    "child": {
				      "type": "object",
				      "description": "The optional child.",
				      "properties": {
				        "value": {
				          "type": "string",
				          "description": "The child value."
				        }
				      },
				      "required": ["value"]
				    }
				  }
				}
				""".trimIndent(),
			).jsonObject

		assertEquals(expectedSchema, jsonSchema<NullableChildHolder>().toJsonElement())
	}
}
