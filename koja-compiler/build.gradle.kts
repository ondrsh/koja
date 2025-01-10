plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
	kotlin("kapt")
}

dependencies {
	compileOnly(libs.auto.service.annotations)
	compileOnly(libs.kotlin.compiler.embeddable)
	kapt(libs.auto.service)
	testImplementation(libs.kotlin.compiler.embeddable)
}
