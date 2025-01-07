plugins {
	alias(libs.plugins.build.config)
}

allprojects {
	version = "kojas-internal"

	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}
