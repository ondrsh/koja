package sh.ondr.koja.outer

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import sh.ondr.koja.Schema
import sh.ondr.koja.initializeKoja
import sh.ondr.koja.toSchema
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
fun inversion(kClass: KClass<*>): Schema {
	initializeKoja()
	return kClass.serializer().descriptor.toSchema()
}
