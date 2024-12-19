import com.vanniktech.maven.publish.SonatypeHost
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
	iosSimulatorArm64()
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

mavenPublishing {
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
	signAllPublications()

	coordinates("sh.ondr", "kotlin-json-schema", project.version.toString())

	pom {
		name = "kotlin-json-schema"
		description = "A Kotlin Multiplatform library for generating JSON Schemas from @Serializable classes."
		inceptionYear = "2024"
		url = "https://github.com/ondrsh/kotlin-json-schema"
		licenses {
			license {
				name = "Apache License 2.0"
				url = "https://www.apache.org/licenses/LICENSE-2.0"
				distribution = "repo"
			}
		}
		developers {
			developer {
				id = "ondrsh"
				name = "Andreas Toth"
				url = "https://github.com/ondrsh"
			}
		}
		scm {
			url = "https://github.com/ondrsh/kotlin-json-schema"
			connection = "scm:git:git://github.com/ondrsh/kotlin-json-schema.git"
			developerConnection = "scm:git:ssh://git@github.com/ondrsh/kotlin-json-schema.git"
		}
	}
}
