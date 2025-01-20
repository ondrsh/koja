package sh.ondr.koja.ksp

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode

data class ValidationContext(
	val blameNode: KSNode,
	val functionOriginNode: KSNode? = null,
	val rootFile: KSFile? = null, // if functionOriginNode is not null, this is the generated file
) {
	fun chooseBlameNode(): KSNode {
		val currentFile = blameNode.containingFile
		val isGenerated = functionOriginNode != null
		return if (isGenerated && currentFile == rootFile) {
			functionOriginNode
		} else {
			blameNode
		}
	}
}
