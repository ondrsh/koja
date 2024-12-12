import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android)
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
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}
	iosArm64()
	iosX64()
	macosArm64()
	js(IR) {
		browser()
		nodejs()
	}
	jvm()

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

android {
	namespace = "sh.ondr.jsonschema"
	compileSdk = 34
	defaultConfig {
		minSdk = 21
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}
