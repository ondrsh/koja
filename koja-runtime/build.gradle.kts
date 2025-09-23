import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.dokka)
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

	// iOS and macOS targets can only be built on macOS
	if (HostManager.hostIsMac) {
		iosArm64()
		iosSimulatorArm64()
		iosX64()
		macosArm64()
		macosX64()
	}

	// Linux targets
	if (HostManager.hostIsLinux) {
		linuxX64()
	}

	// Windows targets
	if (HostManager.hostIsMingw) {
		mingwX64()
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

mavenPublishing {
	configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml")))
}
