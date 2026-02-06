import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.kotlin.dsl.withType

plugins {
	alias(libs.plugins.gradle.versions)
	alias(libs.plugins.kotlin.jvm).apply(false)
	alias(libs.plugins.kotlin.multiplatform).apply(false)
	alias(libs.plugins.maven.publish).apply(false)
	alias(libs.plugins.ondrsh.koja).apply(false)
	alias(libs.plugins.spotless)
	alias(libs.plugins.dokka)
}

allprojects {
	version = property("VERSION_NAME") as String

	configurations.configureEach {
		resolutionStrategy.dependencySubstitution {
			substitute(module("sh.ondr.koja:koja-compiler"))
				.using(project(":koja-compiler"))
		}
	}
	apply(plugin = "com.diffplug.spotless")

	spotless {
		kotlin {
			target("**/*.kt")
			targetExclude("**/build/**/*.kt")
			ktlint()
			// Force Unix line endings on all platforms
			lineEndings = com.diffplug.spotless.LineEnding.UNIX
		}
		kotlinGradle {
			target("**/*.gradle.kts")
			ktlint()
			lineEndings = com.diffplug.spotless.LineEnding.UNIX
		}
	}
}

// Configure Dokka v2 aggregation - only user-facing modules
dependencies {
	dokka(project(":koja-runtime")) // Main API: @JsonSchema, jsonSchema<T>()
	dokka(project(":koja-gradle")) // Gradle plugin: id("sh.ondr.koja")
	// Internal modules (koja-ksp, koja-compiler) are not included in public docs
}

dokka {
	dokkaPublications.html {
		outputDirectory.set(rootDir.resolve("build/dokka/html"))
	}
}

tasks.withType<DependencyUpdatesTask> {
	rejectVersionIf {
		isNonStable(candidate.version)
	}
}

fun isNonStable(version: String): Boolean {
	val upperVersion = version.uppercase()
	val unstableKeywords =
		listOf(
			"ALPHA",
			"BETA",
			"RC",
			"CR",
			"M",
			"PREVIEW",
			"SNAPSHOT",
			"DEV",
			"PRE",
			"BUILD",
			"NIGHTLY",
			"CANARY",
			"EAP",
			"MILESTONE",
		)

	return unstableKeywords.any { upperVersion.contains(it) }
}
