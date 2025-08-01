package sh.ondr.koja.ksp

import com.google.devtools.ksp.symbol.KSType

data class ParamInfo(
	val name: String,
	val fqnType: String,
	val ksType: KSType,
	val isNullable: Boolean,
	val hasDefault: Boolean,
	val isRequired: Boolean,
	val description: String? = null,
)
