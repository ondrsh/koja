package sh.ondr.koja.ksp

import com.google.devtools.ksp.symbol.KSType

/**
 * Converts a KSType into a fully-qualified type string, including its type arguments.
 * For example, a KSType representing List<String> would become "kotlin.collections.List<kotlin.String>".
 */
fun KSType.toFqnString(): String {
	val decl = this.declaration.qualifiedName?.asString() ?: this.toString()
	if (this.arguments.isEmpty()) {
		// No type arguments
		return decl + (if (this.isMarkedNullable) "?" else "")
	}

	// If there are type arguments, reconstruct them
	val args = arguments.joinToString(", ") { arg ->
		val t = arg.type?.resolve()
		t?.toFqnString() ?: "*"
	}

	val nullableMark = if (this.isMarkedNullable) "?" else ""
	return "$decl<$args>$nullableMark"
}

fun String.toCamelCase(): String = this.split(".").joinToString("") { it.replaceFirstChar { it.uppercase() } }
