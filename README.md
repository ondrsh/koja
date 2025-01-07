# kojas - JSON schema generator for Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/sh.ondr/kojas.svg?color=blue)](https://search.maven.org/artifact/sh.ondr/kojas)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A Kotlin Multiplatform library that generates JSON Schemas from <code>@Serializable</code> classes by inspecting kotlinx.serialization metadata at runtime.


## Features
- Generates JSON Schemas from <code>@Serializable</code> classes
- Handles primitives, arrays, maps, nested objects, and enums
- Supports nullable fields and defaults
- Works on multiple platforms (JVM, JS, Native, iOS, Android)


## Installation
Add the dependency:
```
dependencies {
  implementation("sh.ondr:kojas:0.2.0")
}
```


## Usage
Annotate classes with <code>@Serializable</code> and call <code>jsonSchema&lt;YourClass&gt;()</code>.

Example:
```kotlin
@Serializable
data class Person(
  val name: String,
  val age: Int?,
  val emails: List<String> = emptyList(),
)

val schema = jsonSchema<Person>()
println(schema.toJsonElement())
```

Output:

```kotlin
{
  "type": "object",
  "properties": {
    "name": {
      "type": "string"
    },
    "age": {
      "type": "number"
    },
    "emails": {
      "type": "array",
      "items": {
        "type": "string"
      }
    }
  },
  "required": ["name"]
}
```


In this case `email` is optional because it has a default, `age` is optional because it is nullable. Make sure to set `explicitNulls = false` when deserializing as this will populate missing nullable properties with `null`.


Nested Objects and Maps Example:

```kotlin
@Serializable
data class Address(
  val city: String,
  val zip: Int,
)

@Serializable
data class Company(
  val name: String,
  val employees: Map<String, Int>,
  val headquarters: Address,
)

val companySchema = jsonSchema<Company>()
println(companySchema.toJsonElement())
```

Output:

```kotlin
{
  "type": "object",
  "properties": {
    "name": { "type": "string" },
    "employees": {
      "type": "object",
      "additionalProperties": { "type": "number" }
    },
    "headquarters": {
      "type": "object",
      "properties": {
        "city": { "type": "string" },
        "zip": { "type": "number" }
      },
      "required": ["city", "zip"]
    }
  },
  "required": ["name", "employees", "headquarters"]
}
```


Enums become <code>string</code> schemas with <code>enum</code> arrays.


## TODO
- ✏️ Configurable handling of nullable types
- 📝 Property descriptions
- 🔧 Advanced validation keywords (e.g. patterns)
- 🔗 References
- 🔢 Fine-grained number types (integers vs numbers)
- 🔍 Validate map key types


## Contributing
Issues and pull requests are welcome.

## License
Licensed under the [Apache License 2.0](./LICENSE).
