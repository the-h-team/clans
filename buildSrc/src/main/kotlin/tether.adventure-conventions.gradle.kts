plugins {
    id("tether.java-conventions")
    `java-library`
}

// Provide version as extra property
val adventureApiVersion by extra("4.13.0")

dependencies {
    // api configuration = exposed to consumers
    // adventure-api
    api("net.kyori:adventure-api:$adventureApiVersion")
    // minimessage
    api("net.kyori:adventure-text-minimessage:$adventureApiVersion")
}