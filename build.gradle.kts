plugins {
	alias(libs.plugins.kotlin.jvm).apply(false)
	alias(libs.plugins.kotlin.multiplatform).apply(false)
	alias(libs.plugins.maven.publish).apply(false)
	alias(libs.plugins.spotless)
}

allprojects {
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
