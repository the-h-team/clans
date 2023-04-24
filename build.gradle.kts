@file:Suppress("UnstableApiUsage")

plugins {
    id("tether.java-conventions")
    id("tether.spigot-conventions")
    id("tether.placeholderapi-conventions")
    id("tether.upstream-conventions")
    id("tether.dynmap-conventions")
    id("tether.publish-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks.wrapper {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.ALL
}

dependencies {
    implementation(project(":tether-api"))
    implementation(project(":tether-cli"))
//    implementation("net.kyori:adventure-platform-bukkit:${findProperty("adventureApiVersion")}")
    implementation("com.github.the-h-team:Enterprise:${findProperty("enterpriseVersion")}")
    implementation("com.github.the-h-team.Panther:panther-placeholders:${findProperty("pantherVersion")}")
    implementation("com.github.the-h-team.Labyrinth:labyrinth-gui:${findProperty("labyrinthVersion")}")
    implementation("com.github.the-h-team.Labyrinth:labyrinth-regions:${findProperty("labyrinthVersion")}")
    implementation("com.github.the-h-team.Panther:panther-paste:${findProperty("pantherVersion")}")
    implementation("com.github.the-h-team.Labyrinth:labyrinth-common:${findProperty("labyrinthVersion")}")
    implementation("com.github.the-h-team.Labyrinth:labyrinth-skulls:${findProperty("labyrinthVersion")}")
}

tasks.withType<ProcessResources> {
    // Include all resources...
    filesMatching("plugin.yml") {
        // but only expand properties for the plugin.yml
        expand(project.properties)
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("${rootProject.name}-plugin-${project.version}.jar")
    archiveClassifier.set("plugin")
    dependencies {
        include(project(":tether-api"))
        include(project(":tether-cli"))
    }
}
