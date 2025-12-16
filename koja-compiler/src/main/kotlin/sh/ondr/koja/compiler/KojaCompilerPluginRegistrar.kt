package sh.ondr.koja.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

@OptIn(ExperimentalCompilerApi::class)
class KojaCompilerPluginRegistrar : CompilerPluginRegistrar() {
	override val pluginId: String = "sh.ondr.koja"
	override val supportsK2 = true

	override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
		val logger = configuration[CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE]

		IrGenerationExtension.registerExtension(
			object : IrGenerationExtension {
				override fun generate(
					moduleFragment: IrModuleFragment,
					pluginContext: IrPluginContext,
				) {
					val moduleName = moduleFragment.descriptor.stableName?.asStringStripSpecialMarkers()!!
					val moduleId = normaliseModuleName(moduleName)
					moduleFragment.transform(
						KojaIrTransformer(
							moduleName = moduleId,
							pluginContext = pluginContext,
							logger = logger,
						),
						data = null,
					)
				}
			},
		)
	}
}

fun normaliseModuleName(gradlePath: String): String {
	// 1. drop the root project prefix “root:”
	val withoutRoot = gradlePath.substringAfter(':', gradlePath)

	// 2. change ':' and '-' to '_' and canonicalise
	return withoutRoot
		.replace(Regex("[-:]"), "_")
		.replace(Regex("_+"), "_")
		.trim('_')
}
