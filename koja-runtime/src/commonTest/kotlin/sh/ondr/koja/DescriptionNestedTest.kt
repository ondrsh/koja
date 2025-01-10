@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.koja

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionNestedTest {
	@Serializable @JsonSchema
	data class AddressWithDoc(
		val city: String,
		val zip: String,
	)

	@Serializable @JsonSchema
	data class PersonWithAddress(
		val name: String,
		val address: AddressWithDoc,
	)

	@Test
	fun testNestedObjectDescriptions() {
		KojaRegistry.map[serialDescriptor<AddressWithDoc>().serialName] = KojaMeta(
			description = "Represents an address.",
			parameterDescriptions = mapOf(
				"city" to "The city name.",
				"zip" to "The ZIP code.",
			),
		)

		KojaRegistry.map[serialDescriptor<PersonWithAddress>().serialName] = KojaMeta(
			description = "Represents a person.",
			parameterDescriptions = mapOf(
				"name" to "The person's name.",
				"address" to "The person's address.",
			),
		)

		val actualSchema = jsonSchema<PersonWithAddress>().toJsonElement()

		// Expected JSON schema
		val expectedSchema = Json.parseToJsonElement(
			"""
			{
			  "type": "object",
			  "description": "Represents a person.",
			  "properties": {
			    "name": {
			      "type": "string",
			      "description": "The person's name."
			    },
			    "address": {
			      "type": "object",
			      "description": "The person's address.",
			      "properties": {
			        "city": {
			          "type": "string",
			          "description": "The city name."
			        },
			        "zip": {
			          "type": "string",
			          "description": "The ZIP code."
			        }
			      },
			      "required": ["city", "zip"]
			    }
			  },
			  "required": ["name", "address"]
			}
			""".trimIndent(),
		).jsonObject

		assertEquals(expectedSchema, actualSchema)
	}
}
