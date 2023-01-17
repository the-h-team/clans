repositories {
    maven("https://jitpack.io") {
        name = "jitpack"
        content {
            // Try to explicitly select content
            includeModule("com.github.the-h-team", "Enterprise")
            includeModule("com.github.webbukkit", "dynmap-api")
            includeGroup("com.github.the-h-team.Labyrinth")
//            // Backup rule
//            includeGroupByRegex("com\\.github\\..+") // only include com.github.* groups
        }
    }
}
