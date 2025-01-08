package sh.ondr.kojas.ksp.kdoc

data class KdocDescription(
	val description: String?,
	val parameterDescriptions: Map<String, String>,
)
