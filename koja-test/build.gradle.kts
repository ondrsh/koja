import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ondrsh.koja)
}

kotlin {
	jvmToolchain(11)

	iosArm64()
	iosSimulatorArm64()
	iosX64()
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
	linuxX64()
	macosArm64()
	macosX64()
	mingwX64()

	sourceSets {
		commonMain {
			dependencies {
				implementation(project("koja-test-nested"))
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
