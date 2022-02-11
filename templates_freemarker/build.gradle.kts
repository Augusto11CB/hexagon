
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.dependsOn(tasks.getByPath(":templates:compileTestKotlin"))
val templatesTest: SourceSetOutput = project(":templates").sourceSet("test").output

extra["basePackage"] = "com.hexagonkt.templates.freemarker"

dependencies {
    api(project(":templates"))
    api("org.freemarker:freemarker:${properties["freemarkerVersion"]}")

    testImplementation(templatesTest)
    testImplementation(project(":serialization_jackson_json"))
}
