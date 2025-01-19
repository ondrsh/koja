package sh.ondr.koja.test.inner

import kotlinx.serialization.Serializable
import sh.ondr.koja.JsonSchema
import sh.ondr.koja.outer.OuterEngine

/**
 * Describes a car
 * @property name The car's name
 * @property wheels The number of wheels
 * @property engine The car's engine
 * @property color The car's color
 */
@Serializable
@JsonSchema
data class Car(
	val name: String,
	val wheels: Int,
	val engine: OuterEngine = OuterEngine("V8"),
	val color: Color,
)
