pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
	
	versionCatalogs {
		// Configure the existing libs catalog
		configureEach {
			if (name == "libs") {
				// Allow overriding Kotlin version for testing
				val kotlinVersionOverride = providers
					.gradleProperty("test.kotlin.version")
					.orElse(providers.environmentVariable("TEST_KOTLIN_VERSION"))
				
				if (kotlinVersionOverride.isPresent) {
					val versionValue = kotlinVersionOverride.get()
					println("Overriding Kotlin version to $versionValue for testing")
					
					// Override the kotlin version
					version("kotlin", versionValue)
					
					// Also need to update the plugin versions to match
					plugin("kotlin-multiplatform", "org.jetbrains.kotlin.multiplatform").version(versionValue)
					plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version(versionValue)
					plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization").version(versionValue)
				}
			}
		}
	}
}

rootProject.name = "koja"

include("koja-compiler")
include("koja-gradle")
include("koja-ksp")
include("koja-runtime")
include("koja-test")
include("koja-test:koja-test-nested")

includeBuild("koja-build") {
	dependencySubstitution {
		substitute(module("sh.ondr.koja:koja-gradle")).using(project(":gradle-plugin"))
	}
}
