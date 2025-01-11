package sh.ondr.koja

import kotlinx.serialization.Serializable

fun main() {
	println("hello, world again!")
	println("hello, again and again!")
	val schema = jsonSchema<PersonWithAddressAndCar>()
	val kojaMeta = KojaRegistry.map[PersonWithAddressAndCar::class.qualifiedName!!]
	println(kojaMeta)
	println(schema.toJsonElement())
}

/**
 * Represents a person.
 * @property name The person's name.
 * @property address The person's address
 * @property car The person's car
 */
@JsonSchema @Serializable
data class PersonWithAddressAndCar(
	val name: String,
	val address: Address,
	val car: Car,
)

/**
 * Represents an address.
 * @property city The city name.
 * @property zip The ZIP code.
 */
@JsonSchema @Serializable
data class Address(
	val city: String,
	val zip: String,
)
