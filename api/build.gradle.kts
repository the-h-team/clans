plugins {
    id("clans.java-conventions")
    `java-library`
    id("clans.adventure-conventions")
    id("clans.upstream-conventions")
    id("clans.publish-conventions")
}

setMavenName("Clans API")

dependencies {
    // api configuration = exposed to consumers
    api("com.github.the-h-team.Labyrinth:labyrinth-loci:${findProperty("labyrinthVersion")}")
}
