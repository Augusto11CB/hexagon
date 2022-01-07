
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.web"

dependencies {
    "api"(project(":http_server"))
    "api"(project(":templates"))
    "api"(project(":serialization"))

    "api"("org.jetbrains.kotlinx:kotlinx-html-jvm:${properties["kotlinxHtmlVersion"]}")

    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(project(":templates_pebble"))
    "testImplementation"(project(":serialization_jackson_json"))
}
