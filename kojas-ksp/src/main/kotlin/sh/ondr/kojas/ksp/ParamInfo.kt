package sh.ondr.kojas.ksp

import com.google.devtools.ksp.symbol.KSType

data class ParamInfo(
	val name: String,
	val fqnType: String,
	val readableType: String,
	val ksType: KSType,
	val isNullable: Boolean,
	val hasDefault: Boolean,
	val isRequired: Boolean,
	var description: String? = null,
)
