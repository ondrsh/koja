@file:OptIn(KspExperimental::class)

package sh.ondr.kojas.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Origin
import kotlinx.serialization.Serializable
import sh.ondr.kojas.JsonSchema
import sh.ondr.kojas.ksp.kdoc.parseKdoc

// TODO clean up
class KojasProcessor(
	val codeGenerator: CodeGenerator,
	val logger: KSPLogger,
	private val options: Map<String, String>,
) : SymbolProcessor {
	val originatingFiles = mutableListOf<KSFile>()
	val pkg = "sh.ondr.kojas"
	val kojasMetaPackage = "$pkg.generated.meta"
	val kojasInitializerPackage = "$pkg.generated.initializer"
	val validated = mutableSetOf<KSType>()

	val generatedMetasFqs = mutableSetOf<String>()
	var moduleName: KSName? = null

	override fun process(resolver: Resolver): List<KSAnnotated> {
		moduleName = resolver.getModuleName()
		// Validate and process
		resolver.getSymbolsWithAnnotation("sh.ondr.kojas.JsonSchema")
			.filterIsInstance<KSClassDeclaration>()
			.forEach {
				val error = checkTypeError(it.asStarProjectedType())
				if (error == null) {
					it.process(it.qualifiedName!!.asString())
				} else {
					logger.error("Error processing ${it.qualifiedName?.asString()}: $error")
				}
			}
		// Collect all generated metas
		generatedMetasFqs.addAll(resolver.getDeclarationsFromPackage(kojasMetaPackage).map { it.qualifiedName?.asString()!! })
		return listOf()
	}

	private fun KojasProcessor.checkTypeError(type: KSType): String? {
		// Skip already processed
		if (type in validated) {
			return null
		}

		val decl = type.declaration
		val qName = decl.qualifiedName?.asString()

		// Primitives
		val primitiveTypes = setOf(
			"kotlin.String", "kotlin.Char", "kotlin.Boolean",
			"kotlin.Byte", "kotlin.Short", "kotlin.Int", "kotlin.Long",
			"kotlin.Float", "kotlin.Double",
		)
		if (qName != null && primitiveTypes.contains(qName)) {
			validated.add(type)
			return null
		}

		// Sets
		if (qName == "kotlin.collections.Set") {
			if (type.arguments.size != 1) {
				return "Set must have exactly one type parameter."
			}
			val inner = type.arguments[0].type?.resolve() ?: return "Unable to resolve Set element type."
			val innerError = checkTypeError(inner)
			return if (innerError != null) {
				"Set element type not supported: $innerError"
			} else {
				validated.add(type)
				null
			}
		}

		// Lists
		if (qName == "kotlin.collections.List") {
			if (type.arguments.size != 1) {
				return "List must have exactly one type parameter."
			}
			val inner = type.arguments[0].type?.resolve() ?: return "Unable to resolve List element type."
			val innerError = checkTypeError(inner)
			return if (innerError != null) {
				"List element type not supported: $innerError"
			} else {
				validated.add(type)
				null
			}
		}

		// Maps
		if (qName == "kotlin.collections.Map") {
			if (type.arguments.size != 2) {
				return "Map must have exactly two type parameters (key and value)."
			}
			val keyType = type.arguments[0].type?.resolve() ?: return "Unable to resolve Map key type."
			val valueType = type.arguments[1].type?.resolve() ?: return "Unable to resolve Map value type."

			val keyQName = keyType.declaration.qualifiedName?.asString()
			if (keyQName != "kotlin.String") {
				return "Map key must be String, found: ${keyQName ?: "unknown"}."
			}
			val valueError = checkTypeError(valueType)
			return if (valueError != null) {
				"Map value type not supported: $valueError"
			} else {
				validated.add(type)
				null
			}
		}

		// Enums and classes
		val classDecl = decl as? KSClassDeclaration
		if (classDecl != null) {
			if (classDecl.classKind == ClassKind.ENUM_CLASS) {
				// Enums
				if (decl.isAnnotationPresent(JsonSchema::class) == false) {
					return "Enums must be annotated with @JsonSchema."
				}
				validated.add(type)
				return null
			} else if (classDecl.classKind == ClassKind.CLASS) {
				// Classes
				if (decl.isAnnotationPresent(JsonSchema::class) == false) {
					return "Classes must be annotated with both @JsonSchema and @Serializable."
				}
				if (decl.isAnnotationPresent(Serializable::class) == false) {
					return "Classes must be annotated with both @JsonSchema and @Serializable."
				}

				// Check class properties recursively
				decl.getParamInfos().forEach {
					val error = checkTypeError(it.ksType)
					if (error != null) {
						return "Parameter '${it.name}' of class '${classDecl.qualifiedName?.asString()}' is not supported: $error"
					}
				}
				validated.add(type)
				return null
			}
			return "Type '${classDecl.qualifiedName?.asString()}' is not a supported primitive, collection, enum, or @JsonSchema class."
		}

		return "Type '${qName ?: type.toString()}' is not supported."
	}

	fun KSClassDeclaration.process(root: String) {
		if (origin != Origin.KOTLIN) {
			// Skip files from other modules
			return
		}

		// If there is a KDoc, parse it and generate the KojasMeta
		docString?.let { docString ->
			val kdoc = parseKdoc(
				kdoc = docString,
				parameters = getParamInfos().map { it.name },
			)
			originatingFiles.add(containingFile!!)
			generateKojasMeta(
				fqName = qualifiedName!!.asString(),
				kdoc = kdoc,
			)
		}
	}

	fun KSClassDeclaration.getParamInfos(): List<ParamInfo> {
		return primaryConstructor
			?.parameters
			?.mapIndexed { index, p ->
				ParamInfo(
					name = p.name?.asString() ?: "arg$index",
					fqnType = p.type.resolve().toFqnString(),
					readableType = p.type.resolve().toString(),
					ksType = p.type.resolve(),
					isNullable = p.type.resolve().isMarkedNullable,
					hasDefault = p.hasDefault,
					isRequired = !(p.hasDefault || p.type.resolve().isMarkedNullable),
				)
			} ?: emptyList()
	}

	// Generate module-wide initializer
	override fun finish() {
		generateInitializer()
	}
}
