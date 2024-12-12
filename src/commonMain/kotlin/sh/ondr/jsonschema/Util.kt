@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.jsonschema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

// Opinionated: In this library, a field is required if it doesn't have a default AND is not nullable
fun SerialDescriptor.childRequired(index: Int): Boolean {
	return isElementOptional(index).not() && getElementDescriptor(index).isNullable.not()
}
