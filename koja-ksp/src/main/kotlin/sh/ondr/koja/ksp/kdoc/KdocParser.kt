package sh.ondr.koja.ksp.kdoc

import sh.ondr.koja.KojaMeta
import java.util.Scanner

private val allowedTags = setOf(
	"@param",
	"@property",
	"@return",
)

private fun String.isTag() = startsWith("@")

private fun String.isParamOrProperty() = startsWith("@param") || startsWith("@property")

/**
 * Parses a KDoc into a main description and parameter/property descriptions.
 *
 * Restrictions:
 * - The main description ends when we encounter a @param/@property.
 * - Only tags in [allowedTags] are allowed. Any other `@` usage (e.g. `@author`) causes an error.
 * - @param/@property must be followed by a known parameter name and a description.
 * - Having two @param/@property tags with the same parameter name is an error.
 */
fun parseKdoc(
	kdoc: String,
	parameters: List<String>,
): KojaMeta {
	val scanner = Scanner(kdoc).apply { useDelimiter("[ \t\n]+") }
	val tokens = scanner.tokens().toList()
	tokens.filter { it.startsWith("@") }.forEach { t ->
		if (t !in allowedTags) {
			throw IllegalArgumentException(
				"Unsupported tag '$t' found in JSON schema KDoc. " +
					"Only the following tags are allowed: '${allowedTags.joinToString()}'",
			)
		}
	}

	var index = 0
	// Extract main description: until first tag or EOF
	val mainDescTokens = mutableListOf<String>()
	while (index < tokens.size && tokens[index]!!.isTag() == false) {
		mainDescTokens.add(tokens[index])
		index++
	}
	val mainDescription = mainDescTokens.joinToString(" ").ifBlank { null }

	fun skipToNextTag() {
		index++
		while (index < tokens.size && tokens[index]!!.isTag() == false) {
			index++
		}
	}

	fun readUntilNextTag(): List<String> {
		val result = mutableListOf<String>()
		while (index < tokens.size && tokens[index]!!.isTag() == false) {
			result.add(tokens[index])
			index++
		}
		return result
	}

	// If tokens are left, next tokens is a tag
	val paramDescriptions = mutableMapOf<String, String>()
	while (index < tokens.size) {
		val marker = tokens[index]
		if (marker.isParamOrProperty()) {
			index++ // skip param/property tag
			val content = readUntilNextTag()
			val paramName = content.first()
			if (paramName !in parameters) {
				throw IllegalArgumentException(
					"'$marker $paramName' references unknown parameter.",
				)
			}
			val description = content.drop(1)
			if (description.isEmpty()) {
				throw IllegalArgumentException(
					"'$marker $paramName' has no description.",
				)
			}
			if (paramName in paramDescriptions) {
				throw IllegalArgumentException(
					"Duplicate '$marker $paramName' found in KDoc.",
				)
			}
			paramDescriptions[paramName] = description.joinToString(" ").ifBlank {
				throw IllegalArgumentException(
					"'$marker $paramName' has no description.",
				)
			}
		} else if (marker.isTag()) {
			skipToNextTag()
		}
	}

	return KojaMeta(
		description = mainDescription,
		parameterDescriptions = paramDescriptions,
	)
}
