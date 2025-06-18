package sh.ondr.koja.test.inner

import kotlinx.serialization.Serializable
import sh.ondr.koja.JsonSchema

/**
 * Test case 1: Plain quotes - "hello world"
 * @param input Test parameter with "quotes"
 */
@Serializable @JsonSchema
data class TestPlainQuotes(
	val input: String,
)

/**
 * Test case 2: Backticks with quotes - `echo "hello"`
 * @param command Command with `backticks "and quotes"`
 */
@Serializable @JsonSchema
data class TestBackticksQuotes(
	val command: String,
)

/**
 * Test case 3: Triple quotes - """hello"""
 * @param text Text with """triple quotes"""
 */
@Serializable @JsonSchema
data class TestTripleQuotes(
	val text: String,
)

/**
 * Test case 4: Dollar signs - $variable and ${interpolation}
 * @param template Template with $var and ${expr}
 */
@Serializable @JsonSchema
data class TestDollarSigns(
	val template: String,
)

/**
 * Test case 5: Backslashes - \path\to\file
 * @param path Path with \backslashes\
 */
@Serializable @JsonSchema
data class TestBackslashes(
	val path: String,
)

/**
 * Test case 6: Mixed special characters - "quotes" and $dollars and \backslash
 * @param mixed Mixed with "quotes" and $var and \path
 */
@Serializable @JsonSchema
data class TestMixedSpecialChars(
	val mixed: String,
)

/**
 * Test case 7: Already escaped - \"hello\"
 * @param escaped Already escaped \"text\"
 */
@Serializable @JsonSchema
data class TestAlreadyEscaped(
	val escaped: String,
)
