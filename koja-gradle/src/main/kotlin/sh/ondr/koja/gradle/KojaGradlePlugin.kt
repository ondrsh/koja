package sh.ondr.koja.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinCompilerPluginSupportPlugin::class)
class KojaGradlePlugin : KotlinCompilerPluginSupportPlugin {
	override fun apply(target: Project) {
		val kspDependency = target.getKspDependency()
		val runtimeDependency = target.getRuntimeDependency()

		// Apply in any case
		target.pluginManager.apply("com.google.devtools.ksp")

		// Apply to Kotlin Multiplatform projects
		target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
			val kotlin = target.extensions.getByType(KotlinMultiplatformExtension::class.java)
			// Add runtime to commonMain
			kotlin.sourceSets.getByName("commonMain").dependencies {
				if (target.name != "koja-runtime") {
					implementation(runtimeDependency)
				}
			}

			// Add KSP dependency for all Kotlin targets main compilations
			kotlin.targets.configureEach { kotlinTarget ->
				kotlinTarget.compilations.configureEach { compilation ->
					if (compilation.name == "main" && kotlinTarget.name != "metadata") {
						target.dependencies.add(
							"ksp${kotlinTarget.name.replaceFirstChar { it.uppercase() }}",
							kspDependency,
						)
					}
					if (compilation.name == "test" && kotlinTarget.name != "metadata") {
						target.dependencies.add(
							"ksp${kotlinTarget.name.replaceFirstChar { it.uppercase() }}Test",
							kspDependency,
						)
					}
				}
			}
		}

		// Apply to pure JVM projects
		target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
			val kotlinJvm = target.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)
			// Add runtime to main
			kotlinJvm.sourceSets.getByName("main").dependencies {
				if (target.name != "koja-runtime") {
					implementation(runtimeDependency)
				}
			}

			// Add KSP for main
			target.dependencies.add("ksp", kspDependency)
			// Add KSP for test
			target.dependencies.add("kspTest", kspDependency)
		}
	}

	fun Project.getRuntimeDependency() =
		if (isInternalBuild()) {
			project(":koja-runtime")
		} else {
			"sh.ondr.koja:koja-runtime:${PLUGIN_VERSION}"
		}

	fun Project.getKspDependency() =
		if (isInternalBuild()) {
			project(":koja-ksp")
		} else {
			"sh.ondr.koja:koja-ksp:${PLUGIN_VERSION}"
		}

	fun Project.isInternalBuild() = findProperty("sh.ondr.koja.internal") == "true"

	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

	override fun getCompilerPluginId(): String = "sh.ondr.koja"

	override fun getPluginArtifact(): SubpluginArtifact =
		SubpluginArtifact(
			groupId = "sh.ondr.koja",
			artifactId = "koja-compiler",
			version = PLUGIN_VERSION,
		)

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		val project = kotlinCompilation.target.project
		val isTestCompilation = (kotlinCompilation.name == "test")

		return project.provider {
			buildList {
				SubpluginOption("enabled", "true")
				SubpluginOption("isTestSet", isTestCompilation.toString())
			}
		}
	}
}
