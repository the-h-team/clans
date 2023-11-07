plugins {
    id("clans.java-conventions")
    `java-library`
    id("clans.upstream-conventions")
    id("clans.publish-conventions")
}

val cloudVersion by extra("1.8.3")

dependencies {
    // expose api to consumers
    api(project(getSubproject("api")))
    // cloud (includes cloud-core)
    implementation("cloud.commandframework:cloud-annotations:$cloudVersion")
}