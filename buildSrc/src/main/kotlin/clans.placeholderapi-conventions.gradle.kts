plugins {
    id("clans.java-conventions")
}

val placeholderApiVersion by extra("2.11.6")

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "placeholderapi"
        content {
            includeModule("me.clip", "placeholderapi")
        }
    }
}

dependencies {
    // Mark as compileOnly to prevent shadow from trying to analyze it
    // This is okay because PlaceholderAPI is provided at runtime
    compileOnly("me.clip:placeholderapi:$placeholderApiVersion")
}
