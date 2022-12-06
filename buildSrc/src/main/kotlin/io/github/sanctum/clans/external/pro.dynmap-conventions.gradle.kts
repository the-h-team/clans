plugins {
    id("pro.java-conventions")
    id("pro.jitpack-conventions")
}

val dynmapApiVersion by extra("v2.5")

dependencies {
    implementation("com.github.webbukkit:dynmap-api:$dynmapApiVersion") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
}
