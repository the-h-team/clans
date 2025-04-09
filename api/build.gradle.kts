plugins {
    id("clans.java-conventions")
    `java-library`
    id("clans.upstream-conventions")
    id("clans.publish-conventions")
}

setMavenName("Clans API")

dependencies {
    // api configuration = exposed to consumers
    api("com.github.the-h-team.Labyrinth:labyrinth-loci:${findProperty("labyrinthVersion")}")
    api(libs.adventure.api)
    api(libs.adventure.text.minimessage)
}
