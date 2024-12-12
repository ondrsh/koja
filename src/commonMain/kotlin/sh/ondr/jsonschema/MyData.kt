package sh.ondr.jsonschema

data class MyData(val name: String)

fun main() {
	println(MyData("Hello").name)
}
