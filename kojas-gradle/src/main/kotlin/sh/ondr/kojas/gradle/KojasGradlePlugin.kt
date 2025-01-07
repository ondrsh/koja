package sh.ondr.kojas.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinCompilerPluginSupportPlugin::class)
class KojasGradlePlugin : KotlinCompilerPluginSupportPlugin {
	override fun apply(target: Project) {
		// ...
	}

	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

	override fun getCompilerPluginId(): String = "sh.ondr.kojas"

	override fun getPluginArtifact(): SubpluginArtifact =
		SubpluginArtifact(
			groupId = "sh.ondr.kojas",
			artifactId = "kojas-compiler",
			version = PLUGIN_VERSION,
		)

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		val project = kotlinCompilation.target.project
		return project.provider {
			listOf(SubpluginOption(key = "enabled", value = "true"))
		}
	}
}
