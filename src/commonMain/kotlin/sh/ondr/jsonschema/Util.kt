@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.jsonschema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.add

fun JsonArrayBuilder.addAll(elements: Iterable<String>) {
	elements.forEach {
		add(it)
	}
}

fun SerialDescriptor.childRequired(
	childDescriptor: SerialDescriptor,
	i: Int,
): Boolean {
	return isElementOptional(i).not() && childDescriptor.isNullable.not()
}
