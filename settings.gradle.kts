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

rootProject.name = "kojas"

include("kojas-compiler")
include("kojas-gradle")
include("kojas-ksp")
include("kojas-runtime")

includeBuild("kojas-build") {
	dependencySubstitution {
		substitute(module("sh.ondr.kojas:kojas-gradle")).using(project(":gradle-plugin"))
	}
}
