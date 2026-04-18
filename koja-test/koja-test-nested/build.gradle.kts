import org.jetbrains.kotlin.konan.target.HostManager

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ondrsh.koja)
}

kotlin {
	js(IR) {
		browser {
			testTask {
				useKarma {
					useFirefoxHeadless()
				}
			}
		}
		nodejs()
		binaries.library()
	}
	jvm()

	when {
		HostManager.hostIsMac -> {
			iosArm64()
			iosSimulatorArm64()
			iosX64()
			macosArm64()
			macosX64()
			linuxX64()
			mingwX64()
		}

		HostManager.hostIsLinux -> {
			linuxX64()
		}

		HostManager.hostIsMingw -> {
			mingwX64()
		}
	}

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
