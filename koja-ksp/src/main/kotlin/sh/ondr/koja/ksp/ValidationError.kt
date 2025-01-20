package sh.ondr.koja.ksp

import com.google.devtools.ksp.symbol.KSNode

typealias ValidationResult = ValidationError?

data class ValidationError(
	val message: String,
	val node: KSNode? = null,
)
