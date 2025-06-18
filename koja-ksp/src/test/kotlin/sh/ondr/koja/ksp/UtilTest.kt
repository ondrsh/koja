package sh.ondr.koja.ksp

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilTest {
	@Test
	fun `test escapeForKotlinString with quotes`() {
		assertEquals("\\\"hello world\\\"", "\"hello world\"".escapeForKotlinString())
		assertEquals("\\\"\\\"\\\"triple quotes\\\"\\\"\\\"", "\"\"\"triple quotes\"\"\"".escapeForKotlinString())
	}

	@Test
	fun `test escapeForKotlinString with dollar signs`() {
		assertEquals("\\\$variable", "${'$'}variable".escapeForKotlinString())
		assertEquals("\\\${interpolation}", "${'$'}{interpolation}".escapeForKotlinString())
		assertEquals("\\\$var and \\\${expr}", "${'$'}var and ${'$'}{expr}".escapeForKotlinString())
	}

	@Test
	fun `test escapeForKotlinString with backslashes`() {
		assertEquals("\\\\path\\\\to\\\\file", "\\path\\to\\file".escapeForKotlinString())
		assertEquals("C:\\\\Windows\\\\System32", "C:\\Windows\\System32".escapeForKotlinString())
	}

	@Test
	fun `test escapeForKotlinString with special characters`() {
		assertEquals("line1\\nline2", "line1\nline2".escapeForKotlinString())
		assertEquals("col1\\tcol2", "col1\tcol2".escapeForKotlinString())
		assertEquals("return\\rcarriage", "return\rcarriage".escapeForKotlinString())
	}

	@Test
	fun `test escapeForKotlinString with mixed characters`() {
		val input = "\"quotes\" and ${'$'}dollars and \\backslash\nNewline"
		val expected = "\\\"quotes\\\" and \\\$dollars and \\\\backslash\\nNewline"
		assertEquals(expected, input.escapeForKotlinString())
	}

	@Test
	fun `test escapeForKotlinString with already escaped strings`() {
		// Already escaped strings should be double-escaped
		assertEquals("\\\\\\\"hello\\\\\\\"", "\\\"hello\\\"".escapeForKotlinString())
		assertEquals("\\\\n", "\\n".escapeForKotlinString())
		assertEquals("\\\\\\\$var", "\\${'$'}var".escapeForKotlinString())
	}

	@Test
	fun `test escapeForKotlinString with empty and simple strings`() {
		assertEquals("", "".escapeForKotlinString())
		assertEquals("hello", "hello".escapeForKotlinString())
		assertEquals("123", "123".escapeForKotlinString())
	}
}
