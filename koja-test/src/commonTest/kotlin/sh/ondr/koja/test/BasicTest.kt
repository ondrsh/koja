package sh.ondr.koja.test

import kotlinx.serialization.Serializable
import sh.ondr.koja.JsonSchema
import sh.ondr.koja.KojaEntry
import sh.ondr.koja.jsonSchema
import sh.ondr.koja.outer.inversion
import sh.ondr.koja.test.inner.Car
import sh.ondr.koja.toJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A person defined in the test set
 * @param name The name of the person in the test set
 */
@JsonSchema
@Serializable
data class TestPerson(
	val name: String,
)

class BasicTest {
	@Test
	@KojaEntry
	fun inversionTest() {
		val carSchema = inversion(Car::class)
		assertEquals(
			expected = """
{"type":"object","description":"Describes a car","properties":{"name":{"type":"string","description":"The car's name"},"wheels":{"type":"number","description":"The number of wheels"},"engine":{"type":"object","description":"The car's engine","properties":{"name":{"type":"string","description":"The name of the engine."}},"required":["name"]},"color":{"type":"string","description":"The car's color","enum":["BLUE","RED","GREEN"]}},"required":["name","wheels","color"]}
			""".trimIndent(),
			actual = carSchema.toJsonElement().toString(),
		)
	}

	@Test
	@KojaEntry
	fun directTest() {
		val carSchema = jsonSchema<Car>()
		assertEquals(
			expected = """
{"type":"object","description":"Describes a car","properties":{"name":{"type":"string","description":"The car's name"},"wheels":{"type":"number","description":"The number of wheels"},"engine":{"type":"object","description":"The car's engine","properties":{"name":{"type":"string","description":"The name of the engine."}},"required":["name"]},"color":{"type":"string","description":"The car's color","enum":["BLUE","RED","GREEN"]}},"required":["name","wheels","color"]}
			""".trimIndent(),
			actual = carSchema.toJsonElement().toString(),
		)
	}

	@Test
	@KojaEntry
	fun classInTestSet() {
		val testPersonSchema = jsonSchema<TestPerson>()
		assertEquals(
			expected = """
{"type":"object","description":"A person defined in the test set","properties":{"name":{"type":"string","description":"The name of the person in the test set"}},"required":["name"]}
			""".trimIndent(),
			actual = testPersonSchema.toJsonElement().toString(),
		)
	}
}
