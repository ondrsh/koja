import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.dokka)
}

dependencies {
	compileOnly(libs.kotlin.compiler.embeddable)
	testImplementation(libs.kotlin.compiler.embeddable)
}

mavenPublishing {
	configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml")))
}
