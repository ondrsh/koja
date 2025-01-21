plugins {
	alias(libs.plugins.gradle.versions)
	alias(libs.plugins.kotlin.jvm).apply(false)
	alias(libs.plugins.kotlin.multiplatform).apply(false)
	alias(libs.plugins.maven.publish).apply(false)
	alias(libs.plugins.ondrsh.koja).apply(false)
	alias(libs.plugins.spotless)
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
			ktlint()
		}
		kotlinGradle {
			target("**/*.gradle.kts")
			ktlint()
		}
	}
}
