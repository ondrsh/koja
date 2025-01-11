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
include("koja-test-1")
include("koja-test-2")

includeBuild("koja-build") {
	dependencySubstitution {
		substitute(module("sh.ondr.koja:koja-gradle")).using(project(":gradle-plugin"))
	}
}
