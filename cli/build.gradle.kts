plugins {
    id("clans.java-conventions")
    `java-library`
    id("clans.upstream-conventions")
    id("clans.publish-conventions")
}

setMavenName("Clans CLI")

dependencies {
    // expose api to consumers
    api(project(getSubproject("api")))
}