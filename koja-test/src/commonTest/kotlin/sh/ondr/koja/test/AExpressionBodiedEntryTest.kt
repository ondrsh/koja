package sh.ondr.koja.test

import sh.ondr.koja.KojaEntry
import sh.ondr.koja.jsonSchema
import sh.ondr.koja.toJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class AExpressionBodiedEntryTest {
	@Test
	@KojaEntry
	fun expressionBodiedEntryInitializesRegistry() =
		assertEquals(
			expected = """
{"type":"object","description":"A person defined in the test set","properties":{"name":{"type":"string","description":"The name of the person in the test set"}},"required":["name"]}
			""".trimIndent(),
			actual = jsonSchema<TestPerson>().toJsonElement().toString(),
		)
}
