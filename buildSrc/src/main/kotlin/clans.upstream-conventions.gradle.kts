// We will source our libraries from JitPack
repositories {
    maven("https://jitpack.io") {
        name = "jitpack"
        content {
            includeModule("com.github.the-h-team", "Enterprise")
            includeGroup("com.github.the-h-team.Labyrinth")
            includeGroup("com.github.the-h-team.Panther")
            includeGroup("com.github.Revxrsal.Lamp")
        }
    }
    mavenCentral()
    maven ("https://maven.enginehub.org/repo/")
}

// Define versions as extra properties
val enterpriseVersion by extra("1.5")
val labyrinthVersion by extra("1.9.3")
val pantherVersion by extra("1.0.4-SNAPSHOT")
