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
data class Employee(
	val name: String,
	val salary: Int,
)

@Serializable
@JsonSchema
data class EmployeeListHolder(
	val employees: List<Employee>,
)

class DescriptionListTest {
	@Test
	fun testListOfCustomObjectsWithDescriptions() {
		KojaRegistry.map[serialDescriptor<Employee>().serialName] = KojaMeta(
			description = "Represents an employee.",
			parameterDescriptions = mapOf(
				"name" to "The employee's name.",
				"salary" to "The employee's salary.",
			),
		)

		KojaRegistry.map[serialDescriptor<EmployeeListHolder>().serialName] = KojaMeta(
			description = "Holds a list of employees.",
			parameterDescriptions = mapOf(
				"employees" to "The list of employees.",
			),
		)

		val actualSchema = jsonSchema<EmployeeListHolder>().toJsonElement()
		val expectedSchema = Json.parseToJsonElement(
			"""
			{
			  "type": "object",
			  "description": "Holds a list of employees.",
			  "properties": {
			    "employees": {
			      "type": "array",
			      "description": "The list of employees.",
			      "items": {
			        "type": "object",
			        "description": "Represents an employee.",
			        "properties": {
			          "name": {
			            "type": "string",
			            "description": "The employee's name."
			          },
			          "salary": {
			            "type": "number",
			            "description": "The employee's salary."
			          }
			        },
			        "required": ["name", "salary"]
			      }
			    }
			  },
			  "required": ["employees"]
			}
			""".trimIndent(),
		).jsonObject

		// 5) Compare
		assertEquals(expectedSchema, actualSchema)
	}
}
