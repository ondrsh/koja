# koja — JSON Schema for Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/sh.ondr.koja/koja-gradle.svg?color=blue)](https://search.maven.org/artifact/sh.ondr.koja/koja-gradle)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Koja generates JSON Schemas from `@Serializable` Kotlin classes. Class and parameter descriptions are read from KDoc at compile time and included in the generated schema.

```kotlin
/**
 * A person with a name, age, and email addresses.
 * @param name The person's name.
 * @param age The person's age.
 * @param emails The person's email addresses.
 */
@Serializable @JsonSchema
data class Person(
  val name: String,
  val age: Int?,
  val emails: List<String> = emptyList(),
)

@KojaEntry
fun main() {
  println(jsonSchema<Person>().toJsonElement())
}
```

`// prints this:`

```json
{
  "type": "object",
  "description": "A person with a name, age, and email addresses.",
  "properties": {
    "name":   { "type": "string", "description": "The person's name." },
    "age":    { "type": "number", "description": "The person's age." },
    "emails": { "type": "array",  "description": "The person's email addresses.",
                "items": { "type": "string" } }
  },
  "required": ["name"]
}
```

## Usage

1) Annotate data classes and enums with `@JsonSchema` and `@Serializable`.
2) Call `jsonSchema<T>()`.

When used standalone, mark the application entry point with `@KojaEntry`. This is how koja registers the extracted KDoc before any schema is generated:

```kotlin
@KojaEntry
fun main() { /* ... */ }
```

When used through [mcp4k](https://github.com/ondrsh/mcp4k), this step is handled automatically.

### Nested objects and maps

```kotlin
@Serializable @JsonSchema
data class Address(val city: String, val zip: Int)

@Serializable @JsonSchema
data class Company(
  val name: String,
  val employees: Map<String, Int>,
  val headquarters: Address,
)
```

Maps become `object` schemas with `additionalProperties`. Nested classes become nested `object` schemas.

## Supported types

- Primitives: `String`, `Char`, `Boolean`, `Byte`, `Short`, `Int`, `Long`, `Float`, `Double`
- `List<T>`, `Set<T>` → `array`
- `Map<String, T>` → `object` with `additionalProperties` (string keys only)
- Enums → `string` with an `enum` array
- Nested `@Serializable @JsonSchema` classes
- Nullability and default values determine the `required` list

Platforms: JVM, JS (browser, Node), Native (iOS, macOS, Linux, Windows).

## Installation

```kotlin
plugins {
  kotlin("multiplatform") version "2.3.21" // or kotlin("jvm")
  kotlin("plugin.serialization") version "2.3.21"
  id("sh.ondr.koja") version "0.4.10"
}
```

Koja includes a Kotlin compiler plugin, so the Kotlin version must match exactly:

| Koja        | Kotlin  |
|-------------|---------|
| 0.4.10      | 2.3.21  |
| 0.4.9       | 2.3.20  |
| 0.4.8       | 2.3.10  |
| 0.4.7       | 2.3.0   |
| 0.4.6       | 2.2.21  |
| 0.4.4–0.4.5 | 2.2.20  |
| 0.4.3       | 2.2.10  |
| 0.4.1–0.4.2 | 2.2.0   |

## How it works

1. A KSP processor finds `@JsonSchema` classes, validates their type graph, and captures their KDoc.
2. It emits a per-module `KojaInitializer` that populates a global registry of class and parameter descriptions.
3. A Kotlin IR compiler plugin references that initializer from the function marked `@KojaEntry`.
4. At runtime, `jsonSchema<T>()` walks the kotlinx.serialization descriptor and merges in the registered descriptions.

The KDoc parser accepts only `@param`, `@property`, and `@return`, and fails at compile time if a tag references an unknown parameter name.

## Limitations

- `@SerialName` is not supported — koja identifies types by fully qualified name.
- `Map` keys must be `String`.
- The API is experimental and may change before 1.0.

## License

Apache 2.0. See [LICENSE](./LICENSE).
