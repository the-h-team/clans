rootProject.name = "clans"
sequenceOf(
    "api",
    "cli"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}

plugins {
    // Enables easy resolution of a compatible jdk for subprojects
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        // Adventure
        create("libs") {
            version("adventure", "4.20.0")
            sequenceOf("api", "text-minimessage").map { "adventure-$it" }.forEach {
                library(it, "net.kyori", it).versionRef("adventure")
            }
        }
    }
}