plugins {
    id("pro.java-conventions")
}

val placeholderApiVersion by extra("2.10.9")

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "placeholderapi"
        content {
            includeModule("me.clip", "placeholderapi")
        }
    }
}

dependencies {
    implementation("me.clip:placeholderapi:$placeholderApiVersion")
}
