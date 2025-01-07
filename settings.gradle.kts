pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

rootProject.name = "kojas"

include("kojas-compiler")
include("kojas-gradle")
include("kojas-ksp")
include("kojas-runtime")
