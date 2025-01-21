package sh.ondr.koja.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.isAnnotation
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

@OptIn(UnsafeDuringIrConstructionAPI::class)
class KojaIrTransformer(
	isTest: Boolean,
	private val pluginContext: IrPluginContext,
	private val logger: MessageCollector,
) : IrElementTransformerVoid() {
	val pkg = "sh.ondr.koja"
	val initializerFq = if (isTest) "$pkg.generated.initializer.KojaTestInitializer" else "$pkg.generated.initializer.KojaInitializer"
	private val initializerClassId = ClassId.topLevel(FqName(initializerFq))

	override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
		declaration.transformChildrenVoid()

		val isEntry = declaration.annotations.any { it.isAnnotation(FqName("sh.ondr.koja.KojaEntry")) }
		if (isEntry) {
			val body = declaration.body
			if (body is IrBlockBody) {
				val builder = DeclarationIrBuilder(
					pluginContext,
					declaration.symbol,
					declaration.startOffset,
					declaration.endOffset,
				)
				val initializerSymbol = pluginContext.referenceClass(initializerClassId) ?: error("Could not find KojaInitializer")

				// Prepend reference to initializer
				body.statements.add(
					index = 0,
					element = builder.irGetObject(initializerSymbol),
				)
			}
		}

		return super.visitSimpleFunction(declaration)
	}

	override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
		expression.transformChildrenVoid()

		val callee = expression.symbol.owner
		val isNamedBuilder = callee.constructedClass.name.asString() == "Builder"
		val isInServer = callee.constructedClass.parentClassOrNull
			?.name
			?.asString() == "Server"
		val isInRuntimePackage = callee.constructedClass.parentClassOrNull
			?.packageFqName
			?.asString() == "sh.ondr.mcp4k.runtime"
		if (isNamedBuilder && isInServer && isInRuntimePackage) {
			val initializerSymbol = pluginContext.referenceClass(initializerClassId) ?: error("Could not find KojaInitializer")

			val builder =
				DeclarationIrBuilder(
					generatorContext = pluginContext,
					symbol = callee.symbol,
					startOffset = expression.startOffset,
					endOffset = expression.endOffset,
				)

			return builder.irBlock(expression = expression) {
				+irGetObject(initializerSymbol)
				+expression
			}
		}

		return expression
	}
}
