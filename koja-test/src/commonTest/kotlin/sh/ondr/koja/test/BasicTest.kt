package sh.ondr.koja.test

import sh.ondr.koja.KojaEntry
import sh.ondr.koja.jsonSchema
import sh.ondr.koja.outer.inversion
import sh.ondr.koja.test.inner.Car
import sh.ondr.koja.toJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTest {
	@Test @KojaEntry
	fun inversionTest() {
		val carSchema = inversion(Car::class)
		assertEquals(
			expected = """
{"type":"object","description":"Describes a car","properties":{"name":{"type":"string","description":"The car's name"},"wheels":{"type":"number","description":"The number of wheels"},"engine":{"type":"object","description":"The car's engine","properties":{"name":{"type":"string","description":"The name of the engine."}},"required":["name"]},"color":{"type":"string","description":"The car's color","enum":["BLUE","RED","GREEN"]}},"required":["name","wheels","color"]}
			""".trimIndent(),
			actual = carSchema.toJsonElement().toString(),
		)
	}

	@Test @KojaEntry
	fun directTest() {
		val carSchema = jsonSchema<Car>()
		assertEquals(
			expected = """
{"type":"object","description":"Describes a car","properties":{"name":{"type":"string","description":"The car's name"},"wheels":{"type":"number","description":"The number of wheels"},"engine":{"type":"object","description":"The car's engine","properties":{"name":{"type":"string","description":"The name of the engine."}},"required":["name"]},"color":{"type":"string","description":"The car's color","enum":["BLUE","RED","GREEN"]}},"required":["name","wheels","color"]}
			""".trimIndent(),
			actual = carSchema.toJsonElement().toString(),
		)
	}
}
