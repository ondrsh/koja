pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		mavenLocal()
	}
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		mavenLocal()
	}
}

rootProject.name = "koja"

include("koja-compiler")
include("koja-gradle")
include("koja-ksp")
include("koja-runtime")

includeBuild("koja-build") {
	dependencySubstitution {
		substitute(module("sh.ondr.koja:koja-gradle")).using(project(":gradle-plugin"))
	}
}
