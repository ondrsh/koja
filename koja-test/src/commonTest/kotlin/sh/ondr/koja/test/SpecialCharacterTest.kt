package sh.ondr.koja.test

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import sh.ondr.koja.KojaEntry
import sh.ondr.koja.jsonSchema
import sh.ondr.koja.test.inner.TestAlreadyEscaped
import sh.ondr.koja.test.inner.TestBackslashes
import sh.ondr.koja.test.inner.TestBackticksQuotes
import sh.ondr.koja.test.inner.TestDollarSigns
import sh.ondr.koja.test.inner.TestMixedSpecialChars
import sh.ondr.koja.test.inner.TestPlainQuotes
import sh.ondr.koja.test.inner.TestTripleQuotes
import sh.ondr.koja.toJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class SpecialCharacterTest {
	@Test @KojaEntry
	fun testPlainQuotes() {
		val schema = jsonSchema<TestPlainQuotes>()
		val jsonElement = schema.toJsonElement()
		println("JSON element: $jsonElement")
		println("Description field: ${jsonElement.jsonObject["description"]}")
		val json = jsonElement as JsonObject
		
		assertEquals(
			"Test case 1: Plain quotes - \"hello world\"",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val inputProp = props["input"] as JsonObject
		assertEquals(
			"Test parameter with \"quotes\"",
			inputProp["description"]?.jsonPrimitive?.content,
		)
	}

	@Test @KojaEntry
	fun testBackticksWithQuotes() {
		val schema = jsonSchema<TestBackticksQuotes>()
		println(schema.toJsonElement().jsonObject["description"])
		val json = schema.toJsonElement() as JsonObject
		
		assertEquals(
			"Test case 2: Backticks with quotes - `echo \"hello\"`",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val commandProp = props["command"] as JsonObject
		assertEquals(
			"Command with `backticks \"and quotes\"`",
			commandProp["description"]?.jsonPrimitive?.content,
		)
	}

	@Test @KojaEntry
	fun testTripleQuotes() {
		val schema = jsonSchema<TestTripleQuotes>()
		println(schema.toJsonElement().jsonObject["description"])
		val json = schema.toJsonElement() as JsonObject
		
		assertEquals(
			"Test case 3: Triple quotes - \"\"\"hello\"\"\"",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val textProp = props["text"] as JsonObject
		assertEquals(
			"Text with \"\"\"triple quotes\"\"\"",
			textProp["description"]?.jsonPrimitive?.content,
		)
	}

	@Test @KojaEntry
	fun testDollarSigns() {
		val schema = jsonSchema<TestDollarSigns>()
		println(schema.toJsonElement().jsonObject["description"])
		val json = schema.toJsonElement() as JsonObject
		
		assertEquals(
			"Test case 4: Dollar signs - ${'$'}variable and ${'$'}{interpolation}",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val templateProp = props["template"] as JsonObject
		assertEquals(
			"Template with ${'$'}var and ${'$'}{expr}",
			templateProp["description"]?.jsonPrimitive?.content,
		)
	}

	@Test @KojaEntry
	fun testBackslashes() {
		val schema = jsonSchema<TestBackslashes>()
		println(schema.toJsonElement().jsonObject["description"])
		val json = schema.toJsonElement() as JsonObject
		
		assertEquals(
			"Test case 5: Backslashes - \\path\\to\\file",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val pathProp = props["path"] as JsonObject
		assertEquals(
			"Path with \\backslashes\\",
			pathProp["description"]?.jsonPrimitive?.content,
		)
	}

	@Test @KojaEntry
	fun testMixedSpecialCharacters() {
		val schema = jsonSchema<TestMixedSpecialChars>()
		println(schema.toJsonElement().jsonObject["description"])
		val json = schema.toJsonElement() as JsonObject
		
		assertEquals(
			"Test case 6: Mixed special characters - \"quotes\" and ${'$'}dollars and \\backslash",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val mixedProp = props["mixed"] as JsonObject
		assertEquals(
			"Mixed with \"quotes\" and ${'$'}var and \\path",
			mixedProp["description"]?.jsonPrimitive?.content,
		)
	}

	@Test @KojaEntry
	fun testAlreadyEscaped() {
		val schema = jsonSchema<TestAlreadyEscaped>()
		println(schema.toJsonElement().jsonObject["description"])
		val json = schema.toJsonElement() as JsonObject
		
		// Already escaped quotes should appear as literal backslash-quote in the output
		assertEquals(
			"Test case 7: Already escaped - \\\"hello\\\"",
			json["description"]?.jsonPrimitive?.content,
		)
		
		val props = json["properties"] as JsonObject
		val escapedProp = props["escaped"] as JsonObject
		assertEquals(
			"Already escaped \\\"text\\\"",
			escapedProp["description"]?.jsonPrimitive?.content,
		)
	}
}
