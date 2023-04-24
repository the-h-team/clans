plugins {
    id("pro.java-conventions")
}

val dynmapApiVersion by extra("v2.5")

repositories {
    maven("https://jitpack.io") {
        name = "jitpack-dynmap"
        content {
            includeModule("com.github.webbukkit", "dynmap-api")
        }
    }
}

dependencies {
    implementation("com.github.webbukkit:dynmap-api:$dynmapApiVersion") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
}
