package sh.ondr.koja.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class KojaCompilerPluginRegistrar : CompilerPluginRegistrar() {
	override val supportsK2 = true

	override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
		val logger = configuration[CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE]

		IrGenerationExtension.registerExtension(
			object : IrGenerationExtension {
				override fun generate(
					moduleFragment: IrModuleFragment,
					pluginContext: IrPluginContext,
				) {
					val moduleName = moduleFragment.descriptor.name.asStringStripSpecialMarkers()
					moduleFragment.transform(
						KojaIrTransformer(
							isTest = moduleName.endsWith("_test"),
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
