plugins {
    id("clans.java-conventions")
}

val spigotApiVersion by extra("1.16.1-R0.1-SNAPSHOT")

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigot-repo"
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
            includeModule("net.md-5", "bungeecord-chat")
        }
    }
}

dependencies {
    implementation("org.spigotmc:spigot-api:$spigotApiVersion")
}
