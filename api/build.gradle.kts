plugins {
    id("tether.java-conventions")
    `java-library`
    id("tether.adventure-conventions")
    id("tether.upstream-conventions")
}

dependencies {
    // api configuration = exposed to consumers
    api("com.github.the-h-team.Labyrinth:labyrinth-loci:${findProperty("labyrinthVersion")}")
    implementation("com.github.the-h-team.Panther:panther-common:${findProperty("pantherVersion")}")
}
