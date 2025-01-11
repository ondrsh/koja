package sh.ondr.koja

import kotlinx.serialization.Serializable

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
	val engine: Engine = Engine("V8"),
	val color: Color,
)

/**
 * A color
 */
@JsonSchema
enum class Color {
	BLUE,
	RED,
	GREEN,
}
