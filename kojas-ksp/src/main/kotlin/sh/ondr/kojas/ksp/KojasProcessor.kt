@file:OptIn(KspExperimental::class)

package sh.ondr.kojas.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Origin
import kotlinx.serialization.Serializable
import sh.ondr.kojas.ksp.kdoc.KdocDescription
import sh.ondr.kojas.ksp.kdoc.parseKdoc

class KojasProcessor(
	val codeGenerator: CodeGenerator,
	val logger: KSPLogger,
	private val options: Map<String, String>,
) : SymbolProcessor {
	val kdocs = mutableMapOf<String, KdocDescription>()

	override fun process(resolver: Resolver): List<KSAnnotated> {
		resolver.getSymbolsWithAnnotation("sh.ondr.kojas.JsonSchema")
			.filterIsInstance<KSClassDeclaration>()
			.forEach {
				it.process(it.qualifiedName!!.asString())
			}
		return listOf()
	}

	fun KSClassDeclaration.process(root: String) {
		println("Found annotated class: ${qualifiedName?.asString()}")
		val paramInfos = getParamInfos()
		// If there is a KDoc, parse it and put it into map
		docString?.let { docString ->
			val kdoc = parseKdoc(
				kdoc = docString,
				parameters = paramInfos.map { it.name },
			)
			kdocs[qualifiedName!!.asString()] = kdoc
		}
		// Check for @Serializable children to recurse
		paramInfos
			.filter { it.ksType.declaration.isAnnotationPresent(Serializable::class) }
			.map { it.ksType.declaration }
			.filterIsInstance<KSClassDeclaration>()
			.forEach { childClassDeclaration ->
				// Fail if child class is from another module
				if (childClassDeclaration.origin != Origin.KOTLIN) {
					val childClassFq = childClassDeclaration.qualifiedName!!.asString()
					logger.error("Class ${qualifiedName!!.asString()} has property from another module: $childClassFq")
				}
				// Recurse
				childClassDeclaration.process(root)
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

	override fun finish() {
		kdocs.forEach { (fqName, kdoc) ->
			println("KDoc for $fqName: $kdoc")
		}
	}
}
