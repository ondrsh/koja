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
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import kotlinx.serialization.Serializable
import sh.ondr.kojas.ksp.kdoc.parseKdoc

class KojasProcessor(
	val codeGenerator: CodeGenerator,
	val logger: KSPLogger,
	private val options: Map<String, String>,
) : SymbolProcessor {
	val originatingFiles = mutableListOf<KSFile>()
	val pkg = "sh.ondr.kojas"
	val kojasMetaPackage = "$pkg.generated.meta"
	val kojasInitializerPackage = "$pkg.generated.initializer"

	val generatedMetasFqs = mutableSetOf<String>()
	var moduleName: KSName? = null

	override fun process(resolver: Resolver): List<KSAnnotated> {
		moduleName = resolver.getModuleName()
		resolver.getSymbolsWithAnnotation("sh.ondr.kojas.JsonSchema")
			.filterIsInstance<KSClassDeclaration>()
			.forEach {
				it.process(it.qualifiedName!!.asString())
			}
		generatedMetasFqs.addAll(resolver.getDeclarationsFromPackage(kojasMetaPackage).map { it.qualifiedName?.asString()!! })
		return listOf()
	}

	fun KSClassDeclaration.process(root: String) {
		println("Found annotated class: ${qualifiedName?.asString()}")
		val paramInfos = getParamInfos()

		// Check for @Serializable children to recurse
		// TODO validate
		paramInfos
			.filter { it.ksType.declaration.isAnnotationPresent(Serializable::class) }
			.map { it.ksType.declaration }
			.filterIsInstance<KSClassDeclaration>()
			.forEach { childClassDeclaration ->
				// TODO check for validity down the root
			}

		// If there is a KDoc, parse it and generate the KojasMeta
		docString?.let { docString ->
			val kdoc = parseKdoc(
				kdoc = docString,
				parameters = paramInfos.map { it.name },
			)
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

	override fun finish() {
		generateInitializer()
	}
}
