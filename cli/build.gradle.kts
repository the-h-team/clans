plugins {
    id("clans.java-conventions")
    `java-library`
    id("clans.upstream-conventions")
    id("clans.publish-conventions")
    id("com.github.johnrengelman.shadow")
}

setMavenName("Clans CLI")

val lampVersion by extra("3.1.7")

dependencies {
    // expose api to consumers
    api(project(getSubproject("api")))
    // lamp (all platforms)
    implementation("com.github.Revxrsal.Lamp:common:$lampVersion")
    // lamp (platform-specific)
    implementation("com.github.Revxrsal.Lamp:bukkit:$lampVersion")
}

tasks.jar {
    enabled = false
}

tasks.withType<JavaCompile> { // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("") // replace the default classifier
    dependencies {
        // shade lamp
        include(dependency("com.github.Revxrsal.Lamp:common:$lampVersion"))
        include(dependency("com.github.Revxrsal.Lamp:bukkit:$lampVersion"))
    }
    relocate("revxrsal", "clans.revxrsal")
}
