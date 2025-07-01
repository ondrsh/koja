import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("java-gradle-plugin")
	alias(libs.plugins.build.config)
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
}

dependencies {
	compileOnly(libs.kotlin.compiler.embeddable)
	implementation(libs.kotlin.stdlib)
	compileOnly(libs.kotlin.gradle.api)
	compileOnly(libs.kotlin.gradle.plugin)
	implementation(libs.ksp.gradle.plugin)
}

buildConfig {
	useKotlinOutput {
		internalVisibility = true
		topLevelConstants = true
	}
	packageName("sh.ondr.koja.gradle")
	buildConfigField("String", "PLUGIN_VERSION", "\"$version\"")
	buildConfigField("String", "REQUIRED_KOTLIN_VERSION", "\"${libs.versions.kotlin.get()}\"")
	buildConfigField("String", "REQUIRED_KSP_VERSION", "\"${libs.versions.ksp.api.get()}\"")
}

gradlePlugin {
	plugins {
		create("main") {
			id = "sh.ondr.koja"
			implementationClass = "sh.ondr.koja.gradle.KojaGradlePlugin"
		}
	}
}

// If the root project is NOT 'koja', we must be in `koja-build`
if (rootProject.name != "koja") {
	// Move build directory into `koja-build`
	layout.buildDirectory = file("$rootDir/build/koja-gradle-included")
}

// Only publish from real build
if (rootProject.name == "koja") {
	apply(plugin = "com.vanniktech.maven.publish")
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}
