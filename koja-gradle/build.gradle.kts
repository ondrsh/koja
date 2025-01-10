plugins {
	id("java-gradle-plugin")
	alias(libs.plugins.build.config)
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
}

dependencies {
	compileOnly(libs.auto.service)
	compileOnly(libs.auto.service.annotations)
	compileOnly(libs.kotlin.compiler.embeddable)
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.gradle.api)
	implementation(libs.kotlin.gradle.plugin)
	implementation(libs.ksp.gradle.plugin)
}

buildConfig {
	useKotlinOutput {
		internalVisibility = true
		topLevelConstants = true
	}
	packageName("sh.ondr.koja.gradle")
	buildConfigField("String", "PLUGIN_VERSION", "\"$version\"")
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
