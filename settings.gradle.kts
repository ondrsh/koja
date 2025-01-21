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

rootProject.name = "koja"

include("koja-compiler")
include("koja-gradle")
include("koja-ksp")
include("koja-runtime")
include("koja-test")
include("koja-test:koja-test-nested")

includeBuild("koja-build") {
	dependencySubstitution {
		substitute(module("sh.ondr.koja:koja-gradle")).using(project(":gradle-plugin"))
	}
}
