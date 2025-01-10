@file:OptIn(ExperimentalSerializationApi::class)

package sh.ondr.koja

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@MetaSerializable
annotation class JsonSchema
