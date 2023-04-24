plugins {
    id("pro.java-conventions")
    id("tether.upstream-conventions")
}

val cloudVersion by extra("1.8.3")

dependencies {
    implementation(project(":tether-api"))
    // cloud (includes cloud-core)
    implementation("cloud.commandframework:cloud-annotations:$cloudVersion")
}