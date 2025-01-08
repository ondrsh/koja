plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
}

dependencies {
	implementation(project(":kojas-runtime"))
	implementation(libs.kotlinx.serialization.core)
	implementation(libs.ksp.api)
	testImplementation(gradleTestKit())
	testImplementation(kotlin("test-junit5"))
}

tasks.test {
	useJUnitPlatform()
}
