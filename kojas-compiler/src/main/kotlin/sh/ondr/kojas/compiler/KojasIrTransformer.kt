package sh.ondr.kojas.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

@OptIn(UnsafeDuringIrConstructionAPI::class)
class KojasIrTransformer(
	private val pluginContext: IrPluginContext,
	private val logger: MessageCollector,
) : IrElementTransformerVoid() {
	private val initializerClassId = ClassId.topLevel(FqName("sh.ondr.kojas.generated.initializer.KojaInitializer"))
	private val jsonSchemaFqName = FqName("sh.ondr.kojas.jsonSchema")

	override fun visitCall(expression: IrCall): IrExpression {
		expression.transformChildrenVoid()

		// Check if this is a call to the `jsonSchema()` inline function
		val callee = expression.symbol.owner
		val functionFqName = callee.fqNameWhenAvailable
		if (functionFqName == jsonSchemaFqName) {
			// Found a call to `jsonSchema()`
			val initializerSymbol = pluginContext.referenceClass(initializerClassId) ?: error("Could not find KojaInitializer class")

			// Insert a reference to KojaInitializer before the call
			val builder = DeclarationIrBuilder(
				pluginContext,
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
