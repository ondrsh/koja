import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ondrsh.koja)
}

kotlin {
	iosArm64()
	iosSimulatorArm64()
	iosX64()
	jvm {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}
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
