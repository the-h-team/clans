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
        // TODO
    }
}