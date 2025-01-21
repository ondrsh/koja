@file:OptIn(KspExperimental::class)

package sh.ondr.koja.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getFunctionDeclarationsByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Origin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import sh.ondr.koja.JsonSchema
import sh.ondr.koja.ksp.kdoc.parseKdoc

class KojaProcessor(
	val codeGenerator: CodeGenerator,
	val logger: KSPLogger,
) : SymbolProcessor {
	val originatingFiles = mutableListOf<KSFile>()
	val pkg = "sh.ondr.koja"
	val kojaMetaPackage = "$pkg.generated.meta"
	val kojaInitializerPackage = "$pkg.generated.initializer"
	val validated = mutableSetOf<KSType>()
	val visitingStack = mutableSetOf<KSType>()

	val generatedMetasFqs = mutableSetOf<String>()

	var isTest = false

	override fun process(resolver: Resolver): List<KSAnnotated> {
		if (resolver.getModuleName().getShortName().endsWith("_test")) {
			isTest = true
		}

		// Validate and process
		resolver
			.getSymbolsWithAnnotation("sh.ondr.koja.JsonSchema")
			.filterIsInstance<KSClassDeclaration>()
			.forEach { classDecl ->
				// If it’s from generated code, get the original user node
				val originalNode = classDecl.getOriginalNode(resolver) // null if not generated
				val rootCtx = ValidationContext(
					blameNode = classDecl,
					functionOriginNode = originalNode,
					rootFile = classDecl.containingFile,
				)

				val error = validateType(classDecl.asStarProjectedType(), rootCtx)
				if (error != null) {
					logger.error(
						message = error.message,
						symbol = error.node ?: classDecl,
					)
				} else {
					classDecl.process()
				}
			}

		// Collect all generated metas
		generatedMetasFqs.addAll(resolver.getDeclarationsFromPackage(kojaMetaPackage).map { it.qualifiedName?.asString()!! })
		return listOf()
	}

	// Validates a type recursively, returning `null` if valid or a `ValidationError` otherwise.
	fun validateType(
		type: KSType,
		ctx: ValidationContext,
	): ValidationResult {
		// 1) If we've already validated or are in a cycle, skip
		if (type in validated) return null
		if (type in visitingStack) return null

		visitingStack.add(type)
		val decl = type.declaration
		val qName = decl.qualifiedName?.asString()

		// 2) Handle primitive types quickly
		if (qName in primitiveTypes) {
			visitingStack.remove(type)
			validated.add(type)
			return null
		}

		// 3) Check if it’s a Set, List, or Map
		if (qName == "kotlin.collections.Set") {
			return checkSetType(type, ctx)
		}
		if (qName == "kotlin.collections.List") {
			return checkListType(type, ctx)
		}
		if (qName == "kotlin.collections.Map") {
			return checkMapType(type, ctx)
		}

		// 4) If it’s a class or enum, handle that
		val classDecl = decl as? KSClassDeclaration
		val error = when {
			classDecl == null -> ValidationError(
				"Type '${qName ?: type.toString()}' is not supported.",
				ctx.chooseBlameNode(),
			)

			classDecl.classKind == ClassKind.ENUM_CLASS -> checkEnumType(classDecl, type, ctx)
			classDecl.classKind == ClassKind.CLASS -> checkJsonSchemaClass(classDecl, type, ctx)
			else -> ValidationError(
				"Type '${classDecl.qualifiedName?.asString()}' is not a supported primitive, collection, enum, or @JsonSchema class.",
				ctx.chooseBlameNode(),
			)
		}

		visitingStack.remove(type)
		if (error == null) {
			validated.add(type)
		}
		return error
	}

	// Check that Set has 1 type parameter, then validate the element type.
	fun checkSetType(
		type: KSType,
		ctx: ValidationContext,
	): ValidationError? {
		if (type.arguments.size != 1) {
			return ValidationError("Set must have exactly one type parameter.", ctx.chooseBlameNode())
		}
		val innerType = type.arguments[0].type?.resolve()
			?: return ValidationError("Unable to resolve Set element type.", ctx.chooseBlameNode())

		val innerErr = validateType(innerType, ctx)
		if (innerErr != null) return innerErr
		validated.add(type)
		return null
	}

	// Check that List has 1 type parameter, then validate the element type.
	fun checkListType(
		type: KSType,
		ctx: ValidationContext,
	): ValidationResult {
		if (type.arguments.size != 1) {
			return ValidationError("List must have exactly one type parameter.", ctx.chooseBlameNode())
		}
		val inner = type.arguments[0].type?.resolve()
			?: return ValidationError("Unable to resolve List element type.", ctx.chooseBlameNode())
		val innerError = validateType(inner, ctx)
		if (innerError != null) return innerError
		validated.add(type)
		return null
	}

	// Check that Map has 2 type parameters, then validate the value type.
	fun checkMapType(
		type: KSType,
		ctx: ValidationContext,
	): ValidationResult {
		if (type.arguments.size != 2) {
			return ValidationError("Map must have exactly two type parameters (key and value).", ctx.chooseBlameNode())
		}
		val keyType = type.arguments[0].type?.resolve()
			?: return ValidationError("Unable to resolve Map key type.", ctx.chooseBlameNode())
		val keyQName = keyType.declaration.qualifiedName?.asString()
		if (keyQName != "kotlin.String") {
			return ValidationError("Map key must be String, found: ${keyQName ?: "unknown"}.", ctx.chooseBlameNode())
		}

		val valueType = type.arguments[1].type?.resolve() ?: return ValidationError("Unable to resolve Map value type.", ctx.chooseBlameNode())
		val valueError = validateType(valueType, ctx)
		if (valueError != null) return valueError
		validated.add(type)
		return null
	}

	// Check that an enum is annotated with @JsonSchema.
	fun checkEnumType(
		classDecl: KSClassDeclaration,
		type: KSType,
		ctx: ValidationContext,
	): ValidationResult {
		if (!classDecl.isAnnotationPresent(JsonSchema::class)) {
			return ValidationError("Enums must be annotated with @JsonSchema.", ctx.chooseBlameNode())
		}
		validated.add(type)
		return null
	}

	// Check that a class is annotated with @JsonSchema and @Serializable, and recursively validate all its properties.
	fun checkJsonSchemaClass(
		classDecl: KSClassDeclaration,
		type: KSType,
		ctx: ValidationContext,
	): ValidationResult {
		// Must not have @SerialName so we can get the fqName during runtime
		if (classDecl.isAnnotationPresent(SerialName::class)) {
			return ValidationError("@JsonSchema classes must not be annotated with @SerialName.", ctx.chooseBlameNode())
		}
		// Must have both @JsonSchema and @Serializable
		if (
			!classDecl.isAnnotationPresent(JsonSchema::class) ||
			!classDecl.isAnnotationPresent(Serializable::class)
		) {
			return ValidationError(
				"'${classDecl.simpleName.asString()}' must be annotated with both @JsonSchema and @Serializable.",
				ctx.chooseBlameNode(),
			)
		}

		// Check each property
		for (prop in classDecl.getAllProperties()) {
			if (prop.isAnnotationPresent(SerialName::class)) {
				return ValidationError(
					"Properties of @JsonSchema classes must not be annotated with @SerialName.",
					ctx.chooseBlameNode(),
				)
			}
			// Recursively validate each property type, blaming the property as the use site
			val propError = validateType(
				type = prop.type.resolve(),
				ctx = ctx.copy(blameNode = prop),
			)
			if (propError != null) return propError
		}
		validated.add(type)
		return null
	}

	// ------------------------------------------------------------------

	private val primitiveTypes = setOf(
		"kotlin.String",
		"kotlin.Char",
		"kotlin.Boolean",
		"kotlin.Byte",
		"kotlin.Short",
		"kotlin.Int",
		"kotlin.Long",
		"kotlin.Float",
		"kotlin.Double",
	)

	fun KSClassDeclaration.process() {
		if (origin == Origin.KOTLIN_LIB || origin == Origin.JAVA_LIB) {
			// Skip already compiled files
			return
		}

		// If there is a KDoc, parse it and generate the KojaMeta
		docString?.let { docString ->
			val kdoc = parseKdoc(
				kdoc = docString,
				parameters = getParamInfos().map { it.name },
			)
			originatingFiles.add(containingFile!!)
			generateKojaMeta(
				fqName = qualifiedName!!.asString(),
				kdoc = kdoc,
			)
		}
	}

	fun KSClassDeclaration.getParamInfos(): List<ParamInfo> =
		primaryConstructor
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

	// If this class was generated by some other processor, find the original source that triggered the generation
	fun KSClassDeclaration.getOriginalNode(resolver: Resolver): KSNode? =
		containingFile
			?.declarations
			?.filterIsInstance<KSPropertyDeclaration>()
			?.find { propDecl ->
				propDecl.simpleName.asString() == "${simpleName.asString()}OriginalSource"
			}?.docString
			?.trim()
			?.let { functionFq ->
				// For now, only support functions
				resolver
					.getFunctionDeclarationsByName(
						name = functionFq,
						includeTopLevel = true,
					).firstOrNull()
			}

	// Generate module-wide initializer
	override fun finish() {
		generateInitializer()
	}
}
