# koja - JSON schema generator for Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/sh.ondr.koja/koja-gradle.svg?color=blue)](https://search.maven.org/artifact/sh.ondr.koja/koja-gradle)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)


Annotate classes with <code>@JsonSchema</code> and <code>@Serializable</code>.
Then call <code>jsonSchema&lt;YourClass&gt;()</code>:


```kotlin
/**
 * A person with a name, age, and email addresses.
 * @param name The person's name.
 * @param age The person's age.
 * @param emails The person's email addresses.
 */
@Serializable @JsonSchema
data class PersonWithMail(
  val name: String,
  val age: Int?,
  val emails: List<String> = emptyList(),
)

@KojaEntry
fun main() {
  val schema = jsonSchema<PersonWithMail>()
  println(schema.toJsonElement())
}
```

Output:

```json
{
  "type": "object",
  "description": "A person with a name, age, and email addresses.",
  "properties": {
    "name": {
      "type": "string",
      "description": "The person's name."
    },
    "age": {
      "type": "number",
      "description": "The person's age."
    },
    "emails": {
      "type": "array",
      "description": "The person's email addresses.",
      "items": {
        "type": "string"
      }
    }
  },
  "required": ["name"]
}
```

## Features
- Handles primitives, arrays, maps, nested objects, and enums
- Supports nullable fields and defaults
- Works on multiple platforms (JVM, JS, Native, iOS, Android)
- Uses stricter KDoc subset for improved type-safety (throws compile error when specifying non-existing properties)

The current API is experimental and might change.

## Installation
Add koja and the serialization plugin to your plugins block:

```kotlin
plugins {
  kotlin("multiplatform") version "2.1.0" // or kotlin("jvm")
  kotlin("plugin.serialization") version "2.1.0"
  id("sh.ondr.koja") version "0.4.0"
}
```



## Usage

Koja was primarily written for [mcp4k](https://www.github.com/ondrsh/mcp4k).

If used as a standalone library, you have to mark the application entry point to manually push the collected KDocs into the registry:

```kotlin
@KojaEntry
fun main() {
  // your code here
}
```

### Nested Objects and Maps Example:

```kotlin
@JsonSchema @Serializable
data class Address(
  val city: String,
  val zip: Int,
)

@JsonSchema @Serializable
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
- ✅ Validate map key types
- ✅ Property descriptions (KDocs)
- ✅ Proper escaping or special characters
- ⬜ References
- ⬜ Fine-grained number types (integers vs numbers)
- ⬜ Advanced validation keywords (e.g. patterns)
- ⬜ Configurable handling of nullable types
- ⬜ Property descriptions (Annotations)


## Contributing
Issues and pull requests are welcome.

## License
Licensed under the [Apache License 2.0](./LICENSE).
