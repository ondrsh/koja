plugins {
	alias(libs.plugins.build.config)
}

allprojects {
	version = "koja-internal"

	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}
