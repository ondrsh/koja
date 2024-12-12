import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.gradle.versions)
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.spotless)
}

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

kotlin {
	js(IR) {
		browser()
		nodejs()
		binaries.library()
	}

	jvm {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}

	iosArm64()
	iosX64()

	linuxX64()
	macosArm64()

	sourceSets {
		commonMain {
			dependencies {
				api(libs.kotlinx.serialization.core)
				api(libs.kotlinx.serialization.json)
			}
		}
		commonTest {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}
}
