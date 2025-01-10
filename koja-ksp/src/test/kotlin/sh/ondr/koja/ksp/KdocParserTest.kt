package sh.ondr.koja.ksp

import org.gradle.internal.impldep.junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.assertThrows
import sh.ondr.koja.ksp.kdoc.parseKdoc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KdocParserTest {
	@Test
	fun `main description followed by single param`() {
		val kdoc =
			"""
			This function greets the user
			@param name The name of the user
			""".trimIndent()

		val result = parseKdoc(kdoc, parameters = listOf("name"))
		assertEquals("This function greets the user", result.description)
		assertEquals("The name of the user", result.parameterDescriptions["name"])
		assertEquals(1, result.parameterDescriptions.size)
	}

	@Test
	fun `main description followed by multiple params`() {
		val kdoc =
			"""
			Greets multiple users
			@param name The user's name
			@param age The user's age, must be positive
			@param count Number of times to greet
			""".trimIndent()

		val result = parseKdoc(kdoc = kdoc, parameters = listOf("name", "age", "count"))
		assertEquals("Greets multiple users", result.description)
		assertEquals("The user's name", result.parameterDescriptions["name"])
		assertEquals("The user's age, must be positive", result.parameterDescriptions["age"])
		assertEquals("Number of times to greet", result.parameterDescriptions["count"])
		assertEquals(3, result.parameterDescriptions.size)
	}

	@Test
	fun `empty docstring yields no description or parameters`() {
		val kdoc = ""
		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name"),
		)
		assertNull(result.description)
		assertTrue(result.parameterDescriptions.isEmpty())
	}

	@Test
	fun `only main description without params`() {
		val kdoc = "This function greets the user and does nothing else."
		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name"),
		)
		assertEquals("This function greets the user and does nothing else.", result.description)
		assertTrue(result.parameterDescriptions.isEmpty())
	}

	@Test
	fun `unknown parameter name causes error`() {
		val kdoc =
			"""
			Some function
			@param unknownParam Should fail
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("unknown parameter"))
	}

	@Test
	fun `param declared with no description causes error`() {
		val kdoc =
			"""
			Some function
			@param name
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("has no description"))
	}

	@Test
	fun `multi-line param description`() {
		val kdoc =
			"""
			Some function
			@param name The user's name
			             spanning multiple lines
			             until next marker
			@param age The age
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		val nameDesc = "The user's name spanning multiple lines until next marker"
		assertEquals(nameDesc, result.parameterDescriptions["name"])
		assertEquals("The age", result.parameterDescriptions["age"])
	}

	@Test
	fun `handles excessive whitespace and formatting`() {
		val kdoc =
			"""
			This       function
			greets the user      in a
			very nice way.
			@param name    The    name of
			   the user
			@param age The
			user's age   over multiple lines
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		assertEquals("This function greets the user in a very nice way.", result.description)
		assertEquals("The name of the user", result.parameterDescriptions["name"])
		assertEquals("The user's age over multiple lines", result.parameterDescriptions["age"])
	}

	@Test
	fun `additional characters before @param on line are allowed`() {
		val kdoc =
			"""
			Something
			x @param name The name
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name"),
		)
		// The 'x' becomes part of the main description
		assertEquals("Something x", result.description)
		assertEquals("The name", result.parameterDescriptions["name"])
	}

	@Test
	fun `missing some parameters doesn't cause error`() {
		val kdoc =
			"""
			Something
			@param name The name, but age is missing
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		// 'age' is not described, but that is not an error according to the rules
		assertEquals("Something", result.description)
		assertEquals("The name, but age is missing", result.parameterDescriptions["name"])
		assertEquals(1, result.parameterDescriptions.size)
	}

	@Test
	fun `multiple params on the same line are allowed`() {
		val kdoc =
			"""
			@param name foo @param age foo
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		assertNull(result.description)
		assertEquals("foo", result.parameterDescriptions["name"])
		assertEquals("foo", result.parameterDescriptions["age"])
	}

	@Test
	fun `empty description should fail`() {
		val kdoc =
			"""
			@param name
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("'@param name' has no description."))
	}

	@Test
	fun `param description and return`() {
		val kdoc =
			"""
			@param name This is a name
			@return Returns something
			@param age This is an age
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		assertNull(result.description)
		assertEquals("This is a name", result.parameterDescriptions["name"])
		assertEquals("This is an age", result.parameterDescriptions["age"])
	}

	@Test
	fun `mix param and property`() {
		val kdoc =
			"""
			@property name This is a name
			@param age This is an age
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		assertNull(result.description)
		assertEquals("This is a name", result.parameterDescriptions["name"])
		assertEquals("This is an age", result.parameterDescriptions["age"])
	}

	@Test
	fun `declaring description twice must fail`() {
		val kdoc =
			"""
			Function
			@property name Initial description
			@param name Second description
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("Duplicate '@param name' found in KDoc."))
	}

	@Test
	fun `partially matching parameter name fails`() {
		val kdoc =
			"""
			Docs
			@param nam Something about 'nam'
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("unknown parameter"))
	}

	@Test
	fun `unsupported tag other than param causes error`() {
		val kdoc =
			"""
			Some description
			@param name The name
			@foo Unknown marker should fail
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("Unsupported tag '@foo'"))
	}

	@Test
	fun `handling blank lines in docstring`() {
		val kdoc =
			"""
			
			Description line
			
			@param name The name
			
			
			""".trimIndent()

		val result = parseKdoc(
			kdoc = kdoc,
			parameters = listOf("name", "age"),
		)
		// The blank lines get normalized away
		assertEquals("Description line", result.description)
		assertEquals("The name", result.parameterDescriptions["name"])
		assertEquals(1, result.parameterDescriptions.size)
	}

	@Test
	fun `no parameters but param tag is allowed, no other tags allowed`() {
		val kdoc =
			"""
			Just a description
			@see AnotherFunction
			@since 1.0
			""".trimIndent()

		val ex =
			assertThrows<IllegalArgumentException> {
				parseKdoc(
					kdoc = kdoc,
					parameters = listOf("name"),
				)
			}
		assertTrue(ex.message!!.contains("Unsupported tag '@see'"))
	}
}
