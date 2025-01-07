package sh.ondr.kojas.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class KojasProcessorProvider : SymbolProcessorProvider {
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		return KojasProcessor(
			codeGenerator = environment.codeGenerator,
			logger = environment.logger,
			options = environment.options,
		)
	}
}
