import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.dokka)
}

kotlin {
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
