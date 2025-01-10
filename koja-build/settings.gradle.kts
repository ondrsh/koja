dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}

rootProject.name = "koja-build"

// Re-expose `koja-gradle` project so we can substitute the GAV coords with it when building internally
include(":gradle-plugin")
project(":gradle-plugin").projectDir = file("../koja-gradle")
