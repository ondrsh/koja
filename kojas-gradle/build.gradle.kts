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
	packageName("sh.ondr.kojas.gradle")
	buildConfigField("String", "PLUGIN_VERSION", "\"$version\"")
}

gradlePlugin {
	plugins {
		create("main") {
			id = "sh.ondr.kojas"
			implementationClass = "sh.ondr.kojas.gradle.KojasGradlePlugin"
		}
	}
}

// If the root project is NOT 'kojas', we must be in `kojas-build`
if (rootProject.name != "kojas") {
	// Move build directory into `kojas-build`
	layout.buildDirectory = file("$rootDir/build/kojas-gradle-included")
}

// Only publish from real build
if (rootProject.name == "kojas") {
	apply(plugin = "com.vanniktech.maven.publish")
}
