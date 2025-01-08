package sh.ondr.kojas.ksp

import com.google.devtools.ksp.processing.Dependencies
import sh.ondr.kojas.KojasMeta

fun KojasProcessor.generateKojasMeta(
	fqName: String,
	kdoc: KojasMeta,
) {
	val propertyName = fqName.toCamelCase().replaceFirstChar { it.lowercase() } + "KojasMeta"
	val file = codeGenerator.createNewFile(
		dependencies = Dependencies(
			aggregating = true,
			sources = originatingFiles.toTypedArray(),
		),
		packageName = kojasMetaPackage,
		fileName = propertyName,
	)

	val code = buildString {
		appendLine("// Generated by kojas")
		appendLine("package $kojasMetaPackage")
		appendLine()
		appendLine("val $propertyName =")
		appendLine("  \"${fqName}\" to sh.ondr.kojas.KojasMeta(")
		appendLine("    description = \"${kdoc.description}\",")
		appendLine("    parameterDescriptions = mapOf(")
		appendLine("      ${kdoc.parameterDescriptions.entries.joinToString(",\n      ") { "\"${it.key}\" to \"${it.value}\"" }}")
		appendLine("    )")
		appendLine("  )")
	}

	file.write(code.toByteArray())
	file.close()
}
